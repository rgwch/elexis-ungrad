/*******************************************************************************
 * Copyright (c) 2023, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.inbox.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.ungrad.lucinda.Client3;
import ch.elexis.ungrad.lucinda.Client3.INotifier;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class FilenameMatcher {
	Pattern datePattern, datePattern2;

	public FilenameMatcher() {
		// Patterns like 1.2.1960, 01.02.1960, 1960-02-01, 1960-2-1
		datePattern = Pattern.compile("\\d{1,4}[\\.-]\\d?\\d[\\.-]\\d{1,4}");
	}

	/**
	 * Cut a String from another string
	 * 
	 * @param in   the String probably containing frag
	 * @param frag the fragment to cut out
	 * @return in without frag
	 */
	private String cut(String in, String frag) {
		int idx = in.indexOf(frag);
		if (idx == -1) {
			return in;
		} else {
			String ret = in.substring(0, idx) + in.substring(idx + frag.length());
			return ret;
		}
	}

	/**
	 * Cut a date representation from a String
	 * 
	 * @param in   the String probably containing date
	 * @param date a date as dd.mm.yyyy or yyyy-mm-dd
	 * @return in without date
	 */
	private String cutDate(String in, TimeTool date) {
		String ret = cut(in, date.toString(TimeTool.DATE_GER));
		if (ret.equals(in)) {
			ret = cut(in, date.toString(TimeTool.DATE_ISO));
		}
		return ret;
	}

	/**
	 * Find a "Kontakt" from a String. Tries first to find a date. If a date is
	 * found, find a patient with that birth date If no date is found, try all words
	 * of the text as last name. On Match, find first name.
	 * 
	 * @param text a text to scan for first name, last name and birth date
	 * @return The found Person or null if none was found
	 */
	private Person findKontakt(String text) {
		if (!StringTool.isNothing(text)) {
			List<TimeTool> dates = new LinkedList<TimeTool>();
			Matcher m = datePattern.matcher(text);
			while (m.find()) {
				TimeTool t = new TimeTool(m.group());
				dates.add(t);
			}
			for (TimeTool tt : dates) {
				Person p = checkBirthdate(text, tt);
				if (p != null) {
					return p;
				}
				text = cutDate(text, tt).trim();
			}
			Query<Person> qbe = new Query<Person>(Person.class);
			String[] words = text.split("[^\\wäöüÄÖÜéàè]+");
			for (String word : words) {
				if (word.matches("[\\wäöüÄÖÜéàè]+")) {
					qbe.clear();
					qbe.add(Person.NAME, qbe.EQUALS, word);
					List<Person> result = qbe.execute();
					if (result.size() > 0) {
						for (Person p : result) {
							String vn = p.getVorname();
							if (text.contains(vn)) {
								return p;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Match a file against a mapfile
	 * 
	 * @param dd DocumentDescriptor containing all known data. Will be completed
	 *           with docName, dob, firstname, lastname, name if matched.
	 * @throws Exception
	 */
	private void analyzeMappings(DocumentDescriptor dd) throws Exception {
		String mapfilename = CoreHub.localCfg.get(PreferenceConstants.MAPPINGS, null);
		if (mapfilename != null) {
			File mapfile = new File(mapfilename);
			if (mapfile.exists() && mapfile.canRead()) {
				FilenameMapper fmap = new FilenameMapper(mapfile);
				fmap.map(dd);
				if (dd.concerns == null) {
					Query<Person> qbe = new Query<Person>(Person.class);
					if (dd.dob != null) {
						qbe.add(Person.BIRTHDATE, qbe.EQUALS, dd.dob.toString(TimeTool.DATE_COMPACT));
					}
					if (dd.firstname != null) {
						qbe.startGroup();
						qbe.add(Person.FIRSTNAME, Query.EQUALS, dd.firstname);
						qbe.or();
						qbe.add(Person.NAME, Query.EQUALS, dd.firstname);
						qbe.endGroup();
					}
					if (dd.lastname != null) {
						qbe.startGroup();
						qbe.add(Person.NAME, Query.EQUALS, dd.lastname);
						qbe.or();
						qbe.add(Person.FIRSTNAME, Query.EQUALS, dd.lastname);
						qbe.endGroup();

					}
					List<Person> result = qbe.execute();
					if (result.size() == 1) {
						dd.concerns = result.get(0);
					}
				}
			}
		}
	}

	public void analyzeContents(DocumentDescriptor dd) throws Exception {
		if (CoreHub.localCfg.get(PreferenceConstants.ANALYZE_CONTENTS, false)) {
			Client3 client = new Client3();
			client.analyzeFile(FileTool.readFile(dd.file), new INotifier() {

				@Override
				public boolean received(String text) {
					if (!StringTool.isNothing(text) && text.length() > 3) {
						Person p = checkBirthdate(text, null);
						if (p != null) {
							dd.concerns = p;
							return true;
						}
					}
					return false;
				}
			});
		}
	}

	/**
	 * Try to extract patient, title and date from a file.
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public DocumentDescriptor analyze(final File file) throws Exception {
		DocumentDescriptor dd = new DocumentDescriptor(null, new TimeTool(), file, file.getName());
		File meta = new File(file.getAbsolutePath() + ".meta");
		if (meta.exists() && meta.canRead()) {
			String[] metadata = FileTool.readTextFile(meta).split(",", 2);
			dd.sender = metadata[0].split("@")[0];
			if (metadata.length > 1) {
				dd.subject = metadata[1];
			}
		}
		dd.concerns = findKontakt(dd.subject);
		analyzeMappings(dd);
		if (dd.concerns == null) {
			dd.concerns = findKontakt(dd.filename);
		}
		findDates(dd);
		if (dd.concerns == null) {
			analyzeContents(dd);
		}
		makeFilename(dd);
		return dd;
	}

	/**
	 * Make a Filename from the document title
	 * 
	 * @param dd
	 */
	private void makeFilename(DocumentDescriptor dd) {
		int extpos = dd.filename.lastIndexOf('.');
		String ext = extpos == -1 ? "" : dd.filename.substring(extpos);

		if (dd.docname == null) {
			if (extpos != -1) {
				dd.filename = dd.filename.substring(0, extpos);
			}
			dd.docname = cutDate(dd.filename, dd.docDate);
			if (dd.concerns != null) {
				dd.docname = cutDate(dd.docname, new TimeTool(dd.concerns.getGeburtsdatum()));
				dd.docname = cut(dd.docname, dd.concerns.get(Person.NAME));
				dd.docname = cut(dd.docname, dd.concerns.get(Person.FIRSTNAME));
			}
			dd.docname = dd.docname.replaceAll("[,':;]", " ");
			dd.docname = dd.docname.replaceAll("\\s+", "_");
			while (dd.docname.startsWith("-") || dd.docname.startsWith("_")) {
				dd.docname = dd.docname.substring(1);
			}
			dd.docname = dd.docname.replaceAll("\\(\\)", "").replaceAll("__+", "_");
		}
		if (dd.sender != null) {
			dd.docname = dd.docname.trim() + "_" + dd.sender;
		}

		dd.docname = dd.docDate.toString(TimeTool.DATE_ISO) + "_" + dd.docname.trim();

		while (dd.docname.endsWith("-") || dd.docname.endsWith("_")) {
			dd.docname = dd.docname.substring(0, dd.docname.length() - 1);
		}
		dd.filename = dd.docname + ext.toLowerCase();
	}

	/**
	 * Extract dates from the filename. If a date is found, check if it might be a
	 * birth date of a patient. If so, set the concern. If not, set the docDate. If
	 * more than one date is found, the latest will be the docdate. If None is
	 * found, today is the docdate.
	 * 
	 * @param dd DocumentDescriptor pre-filled with at least filename. Will fill
	 *           dd.concern and dd.docDate as found.
	 */
	public void findDates(DocumentDescriptor dd) {
		TimeTool now = new TimeTool();
		TimeTool cand = new TimeTool();
		Matcher m = datePattern.matcher(dd.filename);
		while (m.find()) {
			TimeTool t = new TimeTool(m.group());
			if (!checkBirthdate(dd, t)) {
				if (t.isBeforeOrEqual(now)) {
					if (cand.isEqual(now) || t.isAfter(cand)) {
						cand = t;
					}
				}
			}
		}
		dd.docDate = new TimeTool(cand);
	}

	private boolean checkBirthdate(DocumentDescriptor dd, TimeTool cand) {
		if (dd.concerns == null) {
			dd.concerns = checkBirthdate(dd.filename, cand);
			if (dd.concerns != null) {
				dd.dob = new TimeTool(dd.concerns.getGeburtsdatum());
			}
		}
		return dd.dob != null ? cand.isEqual(dd.dob) : false;
	}

	/**
	 * Test if a date is the birth date of a patient. Look if we have at least one
	 * patient with that birth date. if we have one patient, return this. If more
	 * than one, try t match firstname and lastname with the string. bIf no patient
	 * is found, return null
	 * 
	 * @param text the text to match lastname and firstname
	 * @param cand the date we try as birth date
	 * @return the person with the given birth date or null.
	 */
	private Person checkBirthdate(String text, TimeTool cand) {
		if(StringTool.isNothing(text)) {
			return null;
		}
		if (cand == null) {
			Matcher m = datePattern.matcher(text);
			if (m.find()) {
				cand = new TimeTool(m.group());
			}else {
				return null;
			}
		}
		Query<Person> qbe = new Query<Person>(Person.class, Person.BIRTHDATE, cand.toString(TimeTool.DATE_COMPACT));
		List<Person> result = qbe.execute();
		if (result.size() == 0) {
			return null;
		} else if (result.size() == 1) {
			return result.get(0);
		} else {
			String[] pp = new String[] { Person.NAME, Person.FIRSTNAME };
			String[] hits = new String[2];
			for (Person hit : result) {
				hit.get(pp, hits);
				if (text.contains(hits[0]) && text.contains(hits[1])) {
					return hit;
				}
			}
		}
		return null;
	}
}

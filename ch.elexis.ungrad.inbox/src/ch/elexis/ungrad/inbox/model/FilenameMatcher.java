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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class FilenameMatcher {
	Pattern datePattern, datePattern2;

	public FilenameMatcher() {
		datePattern = Pattern.compile("\\d{2,4}[\\.-]\\d\\d[\\.-]\\d{2,4}");
	}

	private String cut(String in, String frag) {
		int idx = in.indexOf(frag);
		if (idx == -1) {
			return in;
		} else {
			String ret = in.substring(0, idx) + in.substring(idx + frag.length());
			return ret;
		}
	}

	private String cutDate(String in, TimeTool date) {
		String ret = cut(in, date.toString(TimeTool.DATE_GER));
		if (ret.equals(in)) {
			ret = cut(in, date.toString(TimeTool.DATE_ISO));
		}
		return ret;
	}

	private Person findKontakt(String text) {
		if (!StringTool.isNothing(text)) {
			Query<Person> qbe = new Query<Person>(Person.class);
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
			}
		}
		return null;
	}

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

	public DocumentDescriptor analyze(final File file) throws Exception {
		DocumentDescriptor dd = new DocumentDescriptor(null, new TimeTool(), file, file.getName());
		File meta = new File(file.getAbsolutePath() + ".meta");
		if (meta.exists() && meta.canRead()) {
			String[] metadata = FileTool.readTextFile(meta).split(",", 2);
			if (metadata[0].indexOf('@') == -1) {
				dd.sender = metadata[0];
			}
			if (metadata.length > 1) {
				dd.subject = metadata[1];
			}
		}
		dd.concerns = findKontakt(dd.subject);
		analyzeMappings(dd);
		findDates(dd);
		makeFilename(dd);
		return dd;
	}

	private void makeFilename(DocumentDescriptor dd) {
		if (dd.docname == null) {
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
		dd.filename = dd.docDate.toString(TimeTool.DATE_ISO) + "_" + dd.docname.trim();

		int ext = dd.filename.lastIndexOf('.');
		if (ext > -1) {
			String base = dd.filename.substring(0, ext);
			while (base.endsWith("-") || base.endsWith("_")) {
				base = base.substring(0, base.length() - 1);
			}
			dd.filename = base + dd.filename.substring(ext).toLowerCase();
		}

	}

	/**
	 * Extract dates from the filename. If a date is found, check if it might be a
	 * birthdate of a patient. If so, set the concern. If not, set the docDate. If
	 * more than one date is found, the latest will be the docdate. If None is
	 * found, today is the docdate.
	 * 
	 * @param dd DocumentDescriptor prefilled with at least filename. Will fill
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

	private Person checkBirthdate(String text, TimeTool cand) {
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

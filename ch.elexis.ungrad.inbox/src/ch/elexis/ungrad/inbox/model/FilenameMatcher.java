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

	private void analyzeMappings(DocumentDescriptor dd) throws Exception {
		String mapfilename = CoreHub.localCfg.get(PreferenceConstants.MAPPINGS, null);
		if (mapfilename != null) {
			File mapfile = new File(mapfilename);
			if (mapfile.exists() && mapfile.canRead()) {
				FilenameMapper fmap = new FilenameMapper(mapfile);
				fmap.map(dd);
				if (!StringTool.isNothing(dd.docname)) {
					int extpos = dd.filename.lastIndexOf('.');
					String ext = extpos > -1 ? dd.filename.substring(extpos).toLowerCase() : "";
					if (dd.dob != null && checkBirthdate(dd, dd.dob)) {
						dd.filename = dd.docDate.toString(TimeTool.DATE_ISO) + "_" + dd.docname + ext;
						return;
					} else {
						Query<Person> qbe = new Query<Person>(Person.class);
						qbe.add(Person.FIRSTNAME, Query.EQUALS, dd.firstname);
						qbe.add(Person.NAME, Query.EQUALS, dd.lastname);
						List<Person> result = qbe.execute();
						if (result.size() == 1) {
							dd.concerns = result.get(0);
							dd.filename = dd.docDate.toString(TimeTool.DATE_ISO) + "_" + dd.docname + ext;
							return;
						}
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
		analyzeMappings(dd);
		if (dd.docname == null) {
			findDates(dd);
			String ret = dd.filename;
			ret = cutDate(ret, dd.docDate);
			if (dd.concerns != null) {
				ret = cutDate(ret, new TimeTool(dd.concerns.getGeburtsdatum()));
				ret = cut(ret, dd.concerns.get(Person.NAME));
				ret = cut(ret, dd.concerns.get(Person.FIRSTNAME));
			}
			ret = ret.replaceAll("[,':;]", " ");
			ret = ret.replaceAll("\\s+", "_");
			while (ret.startsWith("-") || ret.startsWith("_")) {
				ret = ret.substring(1);
			}
			ret = ret.replaceAll("\\(\\)", "");
			dd.filename = dd.docDate.toString(TimeTool.DATE_ISO) + "_" + ret.trim();

			int ext = dd.filename.lastIndexOf('.');
			if (ext > -1) {
				String base = dd.filename.substring(0, ext);
				while (base.endsWith("-") || base.endsWith("_")) {
					base = base.substring(0, base.length() - 1);
				}
				dd.filename = base + dd.filename.substring(ext).toLowerCase();
			}
		}
		return dd;
	}

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
		Query<Person> qbe = new Query<Person>(Person.class, Person.BIRTHDATE, cand.toString(TimeTool.DATE_COMPACT));
		List<Person> result = qbe.execute();
		if (result.size() == 0) {
			return false;
		} else if (result.size() == 1) {
			dd.concerns = result.get(0);
			return true;
		} else {
			String[] pp = new String[] { Person.NAME, Person.FIRSTNAME };
			String[] hits = new String[2];
			for (Person hit : result) {
				hit.get(pp, hits);
				if (dd.filename.contains(hits[0]) && dd.filename.contains(hits[1])) {
					dd.concerns = hit;
				}
			}
			return true;
		}
	}
}

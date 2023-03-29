package ch.elexis.ungrad.inbox.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.rgw.tools.TimeTool;

public class FilenameMapper {
	Map<Pattern, String[]> patterns = new HashMap<Pattern, String[]>();

	FilenameMapper(File mappingFile) throws Exception {
		FileReader mappingReader = new FileReader(mappingFile);
		BufferedReader br = new BufferedReader(mappingReader);
		String in;
		while ((in = br.readLine()) != null) {
			String line = in.trim();
			if (line.length() > 0 && !line.startsWith("#")) {
				String[] lr = line.split("=");
				if (lr.length != 2) {
					throw new Exception("Bad map definition");
				}
				Pattern p = Pattern.compile(lr[0]);
				String[] m = lr[1].split(",");
				patterns.put(p, m);
			}
		}
		br.close();
	}

	public Docinfo map(String in) throws Exception {
		Docinfo ret = null;
		for (Pattern p : patterns.keySet()) {
//			System.out.println(p);
			Matcher m = p.matcher(in);
			if (m.find()) {
				ret = new Docinfo();
				String[] repl = patterns.get(p);
				for (int c = 0; c < m.groupCount(); c++) {
					String val = m.group(1 + c);
					switch (repl[c]) {
					case "name":
						String[] names=val.split("[ _]",2);
						if(names.length>1) {
							ret.firstname=names[0];
							ret.lastname=names[1];
						}
						ret.patient = val;
						break;
					case "firstname":
						ret.firstname = val;
						break;
					case "lastname":
						ret.lastname = val;
						break;
					case "title":
						ret.docname = val;
						break;
					case "docdate":
						ret.docDate = parseDate(val);
						break;
					case "dob":
						ret.dob = parseDate(val);
						break;
					default:
						throw new Exception("Unknown identifier " + repl[c]);
					}
				}
				return ret;
			}
		}
		return ret;
	}

	private TimeTool parseDate(String date) {
		if (date.matches("\\d{8,8}")) {
			int y = Integer.parseInt(date.substring(0, 4));
			int m = Integer.parseInt(date.substring(4, 6));
			int d = Integer.parseInt(date.substring(6));
			if (y > 1900 && y < 2100 && m < 13 && d < 32) {
				return makeDate(y, m, d);
			} else {
				if (d > 1900 && d < 2100 && y < 32) {
					return makeDate(d, m, y);
				}
			}
		} else if (date.matches("\\d{6,6}")) {
			int y = Integer.parseInt(date.substring(0, 2));
			int m = Integer.parseInt(date.substring(2, 4));
			int d = Integer.parseInt(date.substring(4));
			if (y < 99 && m < 13 && d < 32) {
				return makeDate(y, m, d);
			} else {
				if (d < 99 && y < 32) {
					return makeDate(d, m, y);
				}
			}
		} else if (date.matches("\\d{1,4}[\\.-]\\d?\\d[\\.-]\\d{1,4}")) {
			return new TimeTool(date);
		}
		return null;

	}

	private TimeTool makeDate(int y, int m, int d) {
		if (y < 100) {
			y = y + 2000;
		}
		String year = String.format("%4d", y);
		String month = String.format("%02d", m);
		String day = String.format("%02d", d);
		return new TimeTool(year + "-" + month + "-" + day);
	}
}

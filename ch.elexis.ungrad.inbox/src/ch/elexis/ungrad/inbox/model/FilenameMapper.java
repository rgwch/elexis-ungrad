package ch.elexis.ungrad.inbox.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			Matcher m = p.matcher(in);
			if (m.find()) {
				ret = new Docinfo();
				String[] repl = patterns.get(p);
				for (int c = 0; c < m.groupCount(); c++) {
					String val = m.group(1 + c);
					switch (repl[c]) {
					case "name":
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
						ret.docDate = val;
						break;
					case "dob":
						ret.dob = val;
						break;
					default:
						throw new Exception("Unknown identifier " + repl[c]);
					}
				}

			}
		}
		return ret;
	}
}

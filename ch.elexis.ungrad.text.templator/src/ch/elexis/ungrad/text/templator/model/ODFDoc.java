package ch.elexis.ungrad.text.templator.model;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ODFDoc {
	private Map<String, String> fields = new HashMap<String, String>();

	public void clear() {
		fields.clear();
	}

	public Map<String, String> parseTemplate(InputStream tmpl) throws Exception {
		ZipInputStream zis = new ZipInputStream(tmpl);
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			if (ze.getName().equals("content.xml")) {
				byte[] cnt = new byte[zis.available()];
				zis.read(cnt);
				Pattern pFields = Pattern.compile("\\[\\[w\\.0-9]+\\]");
				Matcher matcher = pFields.matcher(new String(cnt, "utf-8"));
				while (matcher.find()) {
					String found = matcher.group();
					fields.put(found, found);
				}
			}

		}
		return fields;
	}
}

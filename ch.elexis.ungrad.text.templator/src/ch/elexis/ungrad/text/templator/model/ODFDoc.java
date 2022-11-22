/**
 * Copyright (c) 2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 */

package ch.elexis.ungrad.text.templator.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.elexis.ungrad.StorageController;
import ch.elexis.ungrad.text.templator.ui.OOOProcessorPrefs;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class ODFDoc {
	private Map<String, String> fields = new HashMap<String, String>();
	private byte[] template;
	private String title;

	public void clear() {
		fields.clear();
	}

	public Map<String, String> parseTemplate(Brief brief) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(brief.loadBinary());
		parseTemplate(bais);
		String nt = brief.getBetreff();
		if (StringTool.isNothing(nt)) {
			title="Brief";
		}else {
			title = nt;
		}
	
		Kontakt k=brief.getAdressat();
		if(k!=null) {
			String n1=k.get(Kontakt.FLD_NAME1);
			if(n1!=null) {
				title+="_"+n1;
			}
			String n2=k.get(Kontakt.FLD_NAME2);
			if(n2!=null) {
				title+="_"+n2;
			}
		}
		return fields;
	}

	public Map<String, String> parseTemplate(InputStream tmpl) throws Exception {
		ZipInputStream zis = new ZipInputStream(tmpl);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			zos.putNextEntry(ze);
			if (ze.getName().equals("content.xml") || ze.getName().equals("styles.xml")) {
				byte[] cnt = readStream(zis);
				Pattern pFields = Pattern.compile("\\[[\\s\\w\\.:\\/0-9]+\\]");
				String text = new String(cnt, "utf-8");
				Matcher matcher = pFields.matcher(new String(cnt, "utf-8"));
				while (matcher.find()) {
					String found = matcher.group();
					fields.put(found, found);
				}
				zos.write(cnt);
			} else if (ze.getName().equals("meta.xml")) {
				byte[] meta = readStream(zis);
				String s = new String(meta);
				Pattern pTitle = Pattern.compile("<dc:title>(.+)</dc:title>");
				Matcher m = pTitle.matcher(s);
				if (m.find()) {
					title = m.group(1);
				} else {
					pTitle = Pattern.compile("<meta:template.+?xlink:title=\"(.+?)\"");
					m = pTitle.matcher(s);
					if (m.find()) {
						title = m.group(1);
					}
				}
				zos.write(meta);
			} else {
				FileTool.copyStreams(zis, zos);
			}

		}
		zos.flush();
		zos.close();
		template = baos.toByteArray();
		return fields;
	}

	public Set<Entry<String, String>> getFields() {
		return fields.entrySet();
	}

	public String getField(String name) {
		String ret = fields.get(name);
		if (ret == null) {
			return "";
		} else {
			return ret;
		}
	}

	public void setField(String name, String value) {
		if (!value.startsWith("??")) {
			fields.put(name, value);
		}
	}

	public byte[] asByteArray() throws Exception {
		File f = store();
		return FileTool.readFile(f);
	}

	private File store() throws Exception {
		StorageController sc = new StorageController();
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(template));
		if (StringTool.isNothing(title)) {
			title = "Brief";
		}
		File output = sc.createFile(ElexisEventDispatcher.getSelectedPatient(), title + ".odt");
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			zos.putNextEntry(ze);
			if (ze.getName().equals("content.xml") || ze.getName().equals("styles.xml")) {
				OdfTemplateFilterStream otf = new OdfTemplateFilterStream(this, zos);
				FileTool.copyStreams(zis, otf);
			} else {
				FileTool.copyStreams(zis, zos);
			}
		}
		zos.close();
		zis.close();
		return output;
	}

	public boolean doOutput() {
		try {
			File output = store();
			String cmd = CoreHub.localCfg.get(OOOProcessorPrefs.PREFERENCE_BRANCH + "cmd", "soffice");
			String param = CoreHub.localCfg.get(OOOProcessorPrefs.PREFERENCE_BRANCH + "param", "%");
			int i = param.indexOf('%');
			if (i != -1) {
				param = param.substring(0, i) + output.getAbsolutePath() + param.substring(i + 1);
			}

			Process process = Runtime.getRuntime().exec(new String[] { cmd, param });
			// process.waitFor();

			return true;
		} catch (Exception e) {
			ExHandler.handle(e);
			SWTHelper.alert("OpenOffice Processor", "Problem mit dem Erstellen des Dokuments " + e.getMessage());
		}
		return false;
	}

	private byte[] readStream(InputStream is) throws Exception {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		return buffer.toByteArray();
	}

}

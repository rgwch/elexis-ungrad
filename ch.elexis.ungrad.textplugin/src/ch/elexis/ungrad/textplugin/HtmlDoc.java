/*******************************************************************************
 * Copyright (c) 2022 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/

package ch.elexis.ungrad.textplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.ungrad.pdf.Manager;
import ch.elexis.ungrad.textplugin.preferences.PreferenceConstants;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class HtmlDoc {
	final static String VERSION = "1.0.0";
	String template;
	private String outputFile;
	private Map<String, String> prefilled = new HashMap<String, String>();
	private Map<String, Object> postfilled = new HashMap<String, Object>();

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	class Table {
		Table(String[][] f, int[] c) {
			this.fields = f;
			this.sizes = c;
		}

		String[][] fields;
		int[] sizes;
	}

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	class PositionedText {
		public PositionedText(int x, int y, int w, int h, String text, int adjust) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.text = text;
			this.adjust = adjust;
		}

		int x, y, w, h;
		String text;
		int adjust;
	}

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	class DynPositionedText {
		DynPositionedText(Object pos, String text, int adjust) {
			this.pos = (String) pos;
			this.text = text;
			this.adjust = adjust;
		}

		String pos;
		String text;
		int adjust;
	}

	public Map<String, String> getPrefilled() {
		return prefilled;
	}

	public Map<String, Object> getPostfilled() {
		return postfilled;
	}

	public void setPrefilled(String key, String value) {
		prefilled.put(key, value);
	}

	public void setPostfilled(String key, String value) {
		postfilled.put(key, value);
	}

	public String getFilename() {
		return outputFile;
	}

	private String getPostfilledFieldValue(String key) {
		Object el = postfilled.get(key);
		if (el != null) {
			if (el instanceof Table) {
				return createHtmlTable((Table) el);
			} else if (el instanceof String) {
				return (String) el;
			}
		}
		return "";
	}

	public void applyMatcher(String pattern, ReplaceCallback rcb) {
		String text = template;
		Pattern pat = Pattern.compile(pattern);
		Matcher matcher = pat.matcher(text);
		while (matcher.find()) {
			String found = matcher.group();
			String replacement = ((String) rcb.replace(found)).replaceAll("\\n", "<br />");
			if (!replacement.startsWith("??")) {
				prefilled.put(found, replacement);
			}
		}
	}

	public boolean insertTable(String where, String[][] lines, int[] colSizes) {
		Table table = new Table(lines, colSizes);
		postfilled.put(where, table);
		return true;
	}

	private String createHtmlTable(Table table) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		for (int i = 0; i < table.fields.length; i++) {
			sb.append("<tr>");
			for (int j = 0; j < table.fields[i].length; j++) {
				String processed = table.fields[i][j].replaceAll("[\\n\\r]", "<br />");
				if (table.sizes != null && table.sizes.length == table.fields[i].length) {
					sb.append("<td style=\"width:" + table.sizes[j] + "%\">").append(processed).append("</td>");

				} else {
					sb.append("<td>").append(processed).append("</td>");
				}
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	public Object insertTextAt(int x, int y, int w, int h, String toInsert, int adjust) {
		PositionedText fld = new PositionedText(x, y, w, h, toInsert, adjust);
		String marker = StringTool.unique("textmarker");
		postfilled.put(marker, fld);
		/*
		 * StringBuffer sb = new StringBuffer(); Formatter fmt = new Formatter(sb);
		 * sb.append("<div style=\"position:absolute;left:");
		 * fmt.format("%dmm;top:%dmm;height:%dmm;width:%dmm;\" data-marker=\"%s\">", x,
		 * y, w, h, marker); sb.append(toInsert).append("</div>"); Document parsed =
		 * Jsoup.parse(this.text); Element body = parsed.body();
		 * body.append(sb.toString()); this.text = parsed.outerHtml();
		 */
		return marker;
	}

	public Object insertTextAt(Object marker, String toInsert, int adjust) {
		DynPositionedText dpt = new DynPositionedText(marker, toInsert, adjust);
		String newMarker = StringTool.unique("textmarker");
		postfilled.put(newMarker, dpt);
		return newMarker;

		/*
		 * Document parsed = Jsoup.parse(this.text); Elements els =
		 * parsed.getElementsByAttributeValue("data-marker", (String) marker); Element
		 * el = els.first(); if (el != null) { String newMarker =
		 * StringTool.unique("textmarker"); el.append("<div data-marker=\"" + newMarker
		 * + "\">" + toInsert + "</div"); this.text = parsed.outerHtml(); return
		 * newMarker; } return null;
		 */
	}

	/**
	 * Create a snapshot of the current state and create an (unmodifiable)
	 * pdf-output file based on that snapshot
	 * 
	 * @param printer The printer to output the document. If none is given or it
	 *                doesn't exist: just create pdf
	 * @return the fullpathname of the created pdf
	 * @throws Exception
	 */
	public String doOutput(String printer) throws Exception {
		Manager pdf = new Manager();
		String text = template;
		if (text.startsWith("doctype") || text.startsWith("extends")) {
			text = convertPug(template);
		}

		String filename = new TimeTool().toString(TimeTool.FULL_ISO);
		String prefix = (new TimeTool(prefilled.get("[Datum.heute]"))).toString(TimeTool.DATE_ISO) + "_";
		if (prefilled.containsKey("[Adressat.Name]")) {
			filename = prefix + prefilled.get("[Adressat.Name]") + "_" + prefilled.get("[Adressat.Vorname]");
		} else {
			String name = "Ausgang_";
			Pattern pat = Pattern.compile("<title>(.+)</title>");
			Matcher m = pat.matcher(text);
			if (m.find()) {
				String fn = m.group(1);
				name = fn;
			}
			filename = prefix + name;
		}
		String dirname = prefilled.get("[Patient.Name]") + "_" + prefilled.get("[Patient.Vorname]") + "_"
				+ prefilled.get("[Patient.Geburtsdatum]");

		StringBuilder sb = new StringBuilder();
		sb.append(CoreHub.localCfg.get(PreferenceConstants.DOCUMENT_BASE, "")).append(File.separator)
				.append(dirname.substring(0, 1)).append(File.separator).append(dirname);

		File dir = new File(sb.toString());
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Exception("Could not create directory " + dir.getAbsolutePath());
			}
		}
		for (Entry<String, String> e : prefilled.entrySet()) {
			text = text.replace(e.getKey(), e.getValue());
		}
		for (Entry<String, Object> e : postfilled.entrySet()) {
			text = text.replace(e.getKey(), getPostfilledFieldValue(e.getKey()));
		}
		File htmlFile = new File(dir, filename + ".html");
		FileTool.writeTextFile(htmlFile, text);
		File pdfFile = new File(dir, filename + ".pdf");
		pdf.createPDF(htmlFile, pdfFile);
		outputFile = pdfFile.getAbsolutePath();
		return outputFile;
	}

	public byte[] storeToByteArray() {
		try {
			Map<String, Object> out = new HashMap<String, Object>();
			out.put("template", template);
			out.put("prefilled", prefilled);
			out.put("postfilled", postfilled);
			out.put("HtmlTemplatorVersion", VERSION);
			if (!StringTool.isNothing(outputFile)) {
				out.put("filename", outputFile);
			}
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsBytes(out);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}

	private String checkTemplate(String e) throws Exception{
		String ret=e;
		if (e.startsWith("doctype") || e.startsWith("extends")) {
			ret = convertPug(e);
		}
		if (!ret.contains("ElexisHtmlTemplate")) {
			throw new Exception("Bad file format: ElexisHtmlTemplate not found");
		}
		return ret;
	}
	
	public boolean load(byte[] src, boolean asTemplate) throws Exception {
		if (src[0] == '{') {
			// It's an internal (processed) template or an existing document
			ObjectMapper mapper = new ObjectMapper();
			try {
				Map<String, Object> res = mapper.readValue(src, new TypeReference<Map<String, Object>>() {
				});

				template = (String) res.get("template");
			
				prefilled = (Map<String, String>) res.get("prefilled");
				postfilled = (Map<String, Object>) res.get("postfilled");
				outputFile = (String) res.get("filename");
				String version = (String) res.get("HtmlTemplatorVersion");
				if (StringTool.isNothing(version)) {
					throw new Exception("Bad file format");
				}
				return true;

			} catch (Exception ex) {
				ExHandler.handle(ex);
				return false;
			}
		} else {
			// it's a newly imported HTML template file to process or a foreign file.
			template = (new String(src, "utf-8"));
			String text=checkTemplate(template);
			Pattern post = Pattern.compile("\\[\\w+\\]");
			Matcher matcher = post.matcher(text);
			while (matcher.find()) {
				String found = matcher.group();
				postfilled.put(found, "");
			}
			return true;
		}
	}

	//
	public String convertPug(String pug) throws Exception {
		String dir = CoreHub.localCfg.get(PreferenceConstants.TEMPLATE_DIR, ".") + File.separator + "x";
		Process process = new ProcessBuilder("pug", "-p", dir).start();
		InputStreamReader err = new InputStreamReader(process.getErrorStream());
		BufferedReader burr = new BufferedReader(err);
		InputStreamReader ir = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(ir);
		OutputStreamWriter ow = new OutputStreamWriter(process.getOutputStream());
		ow.write(pug);
		ow.flush();
		ow.close();
		String line;
		StringBuilder sb = new StringBuilder();
		StringBuilder serr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		while ((line = burr.readLine()) != null) {
			serr.append(line);
		}
		String errmsg = serr.toString();
		if (StringTool.isNothing(errmsg)) {
			return sb.toString();
		} else {
			throw new Error(errmsg);
		}
	}

	public String getTemplate() {
		return template;
	}

}

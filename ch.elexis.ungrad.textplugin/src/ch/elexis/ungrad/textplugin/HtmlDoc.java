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

import java.io.*;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.ungrad.pdf.Manager;
import ch.elexis.ungrad.textplugin.preferences.PreferenceConstants;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class HtmlDoc {
	final static String VERSION = "1.0.0";
	private String template;
	private String outputFile;
	String text;
	private Map<String, String> prefilled = new HashMap<String, String>();
	private Map<String, Object> postfilled = new HashMap<String, Object>();

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	class Table{
		Table(String[][] f, int[] c) {
			this.fields = f;
			this.sizes = c;
		}

		String[][] fields;
		int[] sizes;
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
		Pattern pat = Pattern.compile(pattern);
		StringBuffer sb = new StringBuffer();
		Matcher matcher = pat.matcher(text);
		while (matcher.find()) {
			String found = matcher.group();
			String replacement = ((String) rcb.replace(found)).replaceAll("\\n", "<br />");
			prefilled.put(found, replacement);
			if (!replacement.startsWith("**ERROR")) {
				matcher.appendReplacement(sb, replacement);
			} else {
				matcher.appendReplacement(sb, " ");
			}
		}
		matcher.appendTail(sb);
		text = sb.toString();
	}

	public boolean insertTable(String where, String[][] lines, int[] colSizes) {
		Table table = new Table(lines, colSizes);
		postfilled.put(where, table);
		String html = createHtmlTable(table);
		text = text.replace(where, html);
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
		StringBuffer sb = new StringBuffer();
		Formatter fmt = new Formatter(sb);
		String marker = StringTool.unique("textmarker");
		sb.append("<div style=\"position:absolute;left:");
		fmt.format("%dmm;top:%dmm;height:%dmm;width:%dmm;\" data-marker=\"%s\">", x, y, w, h, marker);
		sb.append(toInsert).append("</div>");
		Document parsed = Jsoup.parse(this.text);
		Element body = parsed.body();
		body.append(sb.toString());
		this.text = parsed.outerHtml();
		postfilled.put(marker, toInsert);
		return marker;
	}

	public Object insertTextAt(Object marker, String toInsert, int adjust) {
		Document parsed = Jsoup.parse(this.text);
		Elements els = parsed.getElementsByAttributeValue("data-marker", (String) marker);
		Element el = els.first();
		if (el != null) {
			String newMarker = StringTool.unique("textmarker");
			el.append("<div data-marker=\"" + newMarker + "\">" + toInsert + "</div");
			this.text = parsed.outerHtml();
			return newMarker;
		}
		return null;
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

		sb.setLength(0);
		sb.append(File.separator).append(filename);
		String basename = sb.toString();
		applyMatcher("\\[\\w+\\]", new ReplaceCallback() {

			@Override
			public Object replace(String in) {
				return getPostfilledFieldValue(in);
			}
		});
		// text=text.replaceAll("[<>]", "");
		File htmlFile = new File(dir, basename + ".html");
		FileTool.writeTextFile(htmlFile, text);
		File pdfFile = new File(dir, basename + ".pdf");
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

	private void setTemplate(String e) throws Exception {
		template = e;
		if (e.startsWith("doctype") || e.startsWith("extends")) {
			text = convertPug(e);
		} else {
			text = e;
		}
		if (!text.contains("ElexisHtmlTemplate")) {
			throw new Exception("Bad file format: ElexisHtmlTemplate not found");
		}
	}

	public boolean load(byte[] src, boolean asTemplate) throws Exception {
		if (src[0] == '{') {
			// It's an internal (processed) template or an existing document
			ObjectMapper mapper = new ObjectMapper();
			try {
				Map<String, Object> res = mapper.readValue(src, new TypeReference<Map<String, Object>>() {
				});

				setTemplate((String) res.get("template"));
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
			setTemplate(new String(src, "utf-8"));
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

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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.ungrad.pdf.Manager;
import ch.elexis.ungrad.textplugin.preferences.PreferenceConstants;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class HtmlDoc {
	
	private String text;
	Map<String, String> fields = new HashMap<String, String>();
	
	public void loadTemplate(String filename, String basePath) throws Exception{
		if (StringTool.isNothing(basePath)) {
			basePath = CoreHub.localCfg.get(PreferenceConstants.TEMPLATE_DIR, "");
		}
		File ret = new File(basePath, filename);
		if (!ret.exists()) {
			ret = new File(PlatformHelper.getBasePath(PreferenceConstants.PLUGIN_ID) + "/rsc",
				filename);
			if (!ret.exists() || !ret.canRead()) {
				throw new Exception("Could not read " + ret.getAbsolutePath());
			}
		}
		text = FileTool.readTextFile(ret);
		
	}
	
	public Map<String, String> getFields(){
		return fields;
	}
	
	public void applyMatcher(String pattern, ReplaceCallback rcb){
		Pattern pat = Pattern.compile(pattern);
		StringBuffer sb = new StringBuffer();
		Matcher matcher = pat.matcher(text);
		while (matcher.find()) {
			String found = matcher.group();
			String replacement = ((String) rcb.replace(found)).replaceAll("\\n", "<br />");
			fields.put(found, replacement);
			if (!replacement.startsWith("**ERROR")) {
				matcher.appendReplacement(sb, replacement);
			} else {
				matcher.appendReplacement(sb, " ");
			}
		}
		matcher.appendTail(sb);
		text = sb.toString();
	}
	
	public String compile(){
		return text;
	}
	
	public boolean doOutput(String printer) throws Exception{
		Manager pdf = new Manager();
		String filename = fields.get("[Datum.heute]")+"_"+fields.get("[Adressat.Name]")+"_"+fields.get("[Adressat.Vorname]");
		String dirname = fields.get("[Patient.Name]")+"_"+fields.get("[Patient.Vorname]")+"_"+fields.get("[Patient.Geburtsdatum]");
		
		StringBuilder sb = new StringBuilder();
		sb.append(CoreHub.localCfg.get(PreferenceConstants.DOCUMENT_BASE, ""))
			.append(File.separator).append(dirname.substring(0, 1)).append(File.separator).append(dirname);
		
		File dir = new File(sb.toString());
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Exception("Could not create directory " + dir.getAbsolutePath());
			}
		}
		
		sb.setLength(0);
		sb.append(File.separator).append(filename);
		String basename = sb.toString();
		File htmlFile = new File(dir, basename + ".html");
		FileTool.writeTextFile(htmlFile, text);
		File pdfFile = new File(dir, basename + ".pdf");
		pdf.createPDF(htmlFile, pdfFile);
		return true;
	}
	
	public byte[] storeToByteArray(){
		try {
			return text.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			// Will not happen
			e.printStackTrace();
			return null;
		}
		/*
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsBytes(fields);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		*/
	}
	
	public boolean load(byte[] src, boolean asTemplate) throws Exception{
		text = new String(src, "utf-8");
		if( asTemplate) {
			
		}
		return true;
	}
	
}

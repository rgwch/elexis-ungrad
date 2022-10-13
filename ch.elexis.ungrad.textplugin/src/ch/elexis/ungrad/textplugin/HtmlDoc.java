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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.ungrad.textplugin.preferences.PreferenceConstants;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;


public class HtmlDoc {

	private String orig;
	public String processed;
	Map<String,String> fields=new HashMap();
	private Document jDoc;
	
	public void loadTemplate(String filename, String basePath) throws Exception {
		if(StringTool.isNothing(basePath)) {
			basePath=CoreHub.localCfg.get(PreferenceConstants.TEMPLATE_DIR, "");
		}
		File ret = new File(basePath, filename);
		if (!ret.exists()) {
			ret = new File(PlatformHelper.getBasePath(PreferenceConstants.PLUGIN_ID) + "/rsc", filename);
			if (!ret.exists() || !ret.canRead()) {
				throw new Exception("Could not read " + ret.getAbsolutePath());
			}
		}
		orig = FileTool.readTextFile(ret);
		fields.put("template", filename);
	}

	public void applyMatcher(String pattern, ReplaceCallback rcb) {
			Pattern pat = Pattern.compile(pattern);
			StringBuffer sb = new StringBuffer();
			Matcher matcher = pat.matcher(orig);
			while (matcher.find()) {
				String found = matcher.group();
				String replacement = (String) rcb.replace(found);
				if (!replacement.startsWith("**ERROR")) {
					fields.put(found, replacement);
					matcher.appendReplacement(sb, replacement);
				} else {
					matcher.appendReplacement(sb, " ");
				}
			}
			matcher.appendTail(sb);
			processed=sb.toString();
	}
	
	public Map<String,String> getFields(){
		return fields;
	}
	
	public String compile() {
		return processed;
	}
	public byte[] storeToByteArray() {
		ObjectMapper mapper=new ObjectMapper();
		try {
			return mapper.writeValueAsBytes(fields);
		}catch(Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	public void addField(String name, String value) {
		fields.put(name, value);
	}
	public String load(byte[] src) throws Exception {
		ObjectMapper mapper=new ObjectMapper();
		try {
			fields=mapper.readValue(src,new TypeReference<Map<String, Object>>() {} );
		}catch(Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		String template=fields.get("template");
		
		return orig;
	}
	
}

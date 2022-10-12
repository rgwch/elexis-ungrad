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

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.ungrad.textplugin.preferences.PreferenceConstants;
import ch.rgw.io.FileTool;

public class HtmlDoc {

	String orig;
	String processed;
	Map<String,String> fields=new HashMap();
	
	public String load(String filename) throws Exception {
		File ret = new File(CoreHub.localCfg.get(PreferenceConstants.TEMPLATE_DIR, ""), filename);
		if (!ret.exists()) {
			ret = new File(PlatformHelper.getBasePath(PreferenceConstants.PLUGIN_ID) + "rsc", filename);
			if (!ret.exists() || !ret.canRead()) {
				throw new Exception("Could not read " + ret.getAbsolutePath());
			}
		}
		orig = FileTool.readTextFile(ret);
		return orig;
	}

	public void addField(String name, String value) {
		fields.put(name, value);
	}
	public String load(byte[] src) throws Exception {
		orig = new String(src, "utf-8");
		return orig;
	}
	
	public void setProcessed(String proc) {
		processed=proc;
	}
}

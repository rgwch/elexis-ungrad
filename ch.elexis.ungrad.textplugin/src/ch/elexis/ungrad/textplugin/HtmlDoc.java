package ch.elexis.ungrad.textplugin;

import java.io.File;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.ungrad.textplugin.preferences.PreferenceConstants;
import ch.rgw.io.FileTool;

public class HtmlDoc {

	String orig;
	String processed;
	
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

	public String load(byte[] src) throws Exception {
		orig = new String(src, "utf-8");
		return orig;
	}
	
	public void setProcessed(String proc) {
		processed=proc;
	}
}

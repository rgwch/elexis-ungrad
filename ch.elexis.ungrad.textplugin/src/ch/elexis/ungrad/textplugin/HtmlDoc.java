package ch.elexis.ungrad.textplugin;

import java.io.File;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.ungrad.textplugin.preferences.PreferenceConstants;
import ch.rgw.io.FileTool;

public class HtmlDoc {

	public String load(String filename) throws Exception {
		File ret = new File(CoreHub.localCfg.get(PreferenceConstants.TEMPLATE_DIR, ""), filename);
		if (!ret.exists()) {
			ret = new File(PlatformHelper.getBasePath(PreferenceConstants.PLUGIN_ID) + "rsc", filename);
			if (!ret.exists() || !ret.canRead()) {
				throw new Exception("Could not read " + ret.getAbsolutePath());
			}
		}
		String html = FileTool.readTextFile(ret);
		return html;
	}
}

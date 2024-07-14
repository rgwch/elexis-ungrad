package ch.elexis.ungrad;

import ch.elexis.core.preferences.PreferencesUtil;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.CoreUtil.OS;

public class ExtPreferencesUtil extends PreferencesUtil {
	public static void setOsSpecificPreference(String defaultPreference, String value, IConfigService configService) {
		OS operatingSystem = CoreUtil.getOperatingSystemType();
		String osSpecificPreference = getOsSpecificPreferenceName(operatingSystem, defaultPreference);
		configService.set(osSpecificPreference, value);
	}
}

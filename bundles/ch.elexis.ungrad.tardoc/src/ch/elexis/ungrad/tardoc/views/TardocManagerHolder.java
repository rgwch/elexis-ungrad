package ch.elexis.ungrad.tardoc.views;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Static holder for TardocManager service to make it accessible in Eclipse views
 * that don't support direct OSGi injection.
 */
@Component
public class TardocManagerHolder {
	
	private static TardocManager tardocManager;
	
	@Reference
	public void setTardocManager(TardocManager manager) {
		TardocManagerHolder.tardocManager = manager;
	}
	
	public void unsetTardocManager(TardocManager manager) {
		if (TardocManagerHolder.tardocManager == manager) {
			TardocManagerHolder.tardocManager = null;
		}
	}
	
	public static TardocManager get() {
		return tardocManager;
	}
}

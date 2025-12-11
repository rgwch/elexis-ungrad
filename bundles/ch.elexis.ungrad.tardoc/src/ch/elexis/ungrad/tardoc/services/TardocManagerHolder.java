package ch.elexis.ungrad.tardoc.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/* We have to do quite a lot boilerplate coding, because ch.elexis.base.arzttarife.service.ArzttarifeModelServiceHolder
 * is not API accessible.
 * This is copilot's solution.
 */
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

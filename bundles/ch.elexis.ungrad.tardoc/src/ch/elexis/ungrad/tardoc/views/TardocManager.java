package ch.elexis.ungrad.tardoc.views;


import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

@Component(service = TardocManager.class)
public class TardocManager{
	
	private static IModelService modelService;
		
	public TardocManager() {
		
	}	
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.base.ch.arzttarife.model)")
	public void setModelService(IModelService modelService) {
		TardocManager.modelService = modelService;
	}
	
	/**
	 * Get the IModelService, with fallback to direct OSGi lookup if injection hasn't happened yet.
	 * 
	 * @param context Optional BundleContext to use for service lookup. If null, will try to get from FrameworkUtil.
	 */
	private IModelService getModelService(BundleContext context) {
		if (modelService != null) {
			System.out.println("TardocManager: Using injected IModelService");
			return modelService;
		}
		
		System.out.println("TardocManager: Attempting direct OSGi lookup for IModelService");
		
		// Fallback: try to get service directly from OSGi
		try {
			if (context == null) {
				context = FrameworkUtil.getBundle(getClass()).getBundleContext();
			}
			
			if (context != null) {
				System.out.println("TardocManager: BundleContext obtained");
				String filter = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.base.ch.arzttarife.model)";
				ServiceReference<?>[] refs = context.getServiceReferences(IModelService.class.getName(), filter);
				if (refs != null && refs.length > 0) {
					System.out.println("TardocManager: Found " + refs.length + " matching service(s)");
					IModelService service = (IModelService) context.getService(refs[0]);
					modelService = service; // Cache it
					return service;
				} else {
					System.err.println("TardocManager: No matching IModelService found with filter: " + filter);
				}
			} else {
				System.err.println("TardocManager: BundleContext is null");
			}
		} catch (InvalidSyntaxException e) {
			System.err.println("TardocManager: Invalid filter syntax");
			e.printStackTrace();
		}
		
		return null;
	}
		
	public List<ITardocLeistung> getLeistungen(String filter) {
		return getLeistungen(filter, null);
	}
	
	public List<ITardocLeistung> getLeistungen(String filter, BundleContext context) {
		IModelService service = getModelService(context);
		if (service == null) {
			System.err.println("TardocManager: IModelService is not available");
			return Collections.emptyList();
		}
		
		try {
			IQuery<ITardocLeistung> query = service.getQuery(ITardocLeistung.class);
			
			// Filter by tx255 field - use LIKE for partial matching
			if (filter != null && !filter.trim().isEmpty()) {
				query.and("tx255", COMPARATOR.LIKE, "%" + filter + "%", true);
			}
			
			// Exclude deleted entries
			query.and("deleted", COMPARATOR.EQUALS, false);
			
			// Order by code
			query.orderBy("code_", IQuery.ORDER.ASC);
			
			return query.execute();
		} catch (Exception e) {
			// Handle case where service is not available
			e.printStackTrace();
			return Collections.emptyList();
		}
	}	
	
	/**
	 * Check if the model service is available
	 */
	public boolean isServiceAvailable() {
		return isServiceAvailable(null);
	}
	
	/**
	 * Check if the model service is available
	 * @param context Optional BundleContext
	 */
	public boolean isServiceAvailable(BundleContext context) {
		return getModelService(context) != null;
	}
}
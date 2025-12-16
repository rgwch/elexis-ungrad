/*******************************************************************************
 * Copyright (c) 2025 by G. Weirich
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

package ch.elexis.ungrad.tardoc.services;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung;
import ch.elexis.base.ch.arzttarife.util.ArzttarifeUtil;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;

/**
 * Service to manage Tardoc services and provide filtering based on mandator
 * dignities.
 */
@Component(service = TardocManager.class)
public class TardocManager {

	private static IModelService modelService;
	private boolean bOnlyValidForDignity = true;

	private static TardocManager theInstance = null;

	private TardocManager() {

	}

	public static TardocManager getInstance() {
		if (theInstance == null) {
			theInstance = new TardocManager();
		}
		return theInstance;
	}

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.base.ch.arzttarife.model)")
	public void setModelService(IModelService modelService) {
		TardocManager.modelService = modelService;
	}

	/**
	 * Get the IModelService, with fallback to direct OSGi lookup if injection
	 * hasn't happened yet. We have to do quite a lot boilerplate coding, because
	 * ch.elexis.base.arzttarife.service.ArzttarifeModelServiceHolder is not API
	 * accessible. This is copilot's solution.
	 * 
	 * @param context Optional BundleContext to use for service lookup. If null,
	 *                will try to get from FrameworkUtil.
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

	/**
	 * Get a list of Tardoc services, optionally filtered by a search string on the
	 * text field. if bOnlyValidForDignity is true, only services where DigniQuali
	 * includes at least one of the current mandator's dignities are returned.
	 * 
	 * @param filter Optional search string to filter services by text (tx255 field)
	 * @return The list of matching ITardocLeistung services (possibly empty but
	 *         never null)
	 */
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
			query.and("isChapter", COMPARATOR.EQUALS, false);

			// Filter by tx255 field - use LIKE for partial matching
			if (filter != null && !filter.trim().isEmpty()) {
				query.and("tx255", COMPARATOR.LIKE, "%" + filter + "%", true);
			}

			// Exclude deleted entries
			query.and("deleted", COMPARATOR.EQUALS, false);

			// Order by code
			query.orderBy("code_", IQuery.ORDER.ASC);

			List<ITardocLeistung> results = query.execute();

			// Filter by dignity if required
			if (bOnlyValidForDignity) {
				results = filterByDignity(results);
			}

			return results;
		} catch (Exception e) {
			// Handle case where service is not available
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/**
	 * Filter the list of Tardoc services based on the current mandator's dignities.
	 * Only services where DigniQuali includes at least one of the mandator's
	 * dignities are returned.
	 * 
	 * @param leistungen List of services to filter
	 * @return Filtered list of services
	 */
	private List<ITardocLeistung> filterByDignity(List<ITardocLeistung> leistungen) {
		// Get current mandator
		IMandator mandator = ContextServiceHolder.get().getActiveMandator().orElse(null);
		if (mandator == null) {
			return leistungen;
		}

		// Get mandator's dignities
		List<ICoding> mandatorDignities = ArzttarifeUtil.getMandantTardocSepcialist(mandator);
		if (mandatorDignities == null || mandatorDignities.isEmpty()) {
			return leistungen;
		}

		// Extract dignity codes
		Set<String> mandatorDignityCodes = mandatorDignities.stream().map(ICoding::getCode).collect(Collectors.toSet());

		// Filter services
		return leistungen.stream().filter(leistung -> {
			String digniQuali = leistung.getDigniQuali();
			if (digniQuali == null || digniQuali.trim().isEmpty()) {
				// If no dignity specified, include it (or exclude based on your business logic)
				return true;
			}

			// Split by pipe and check if any dignity matches
			String[] dignities = digniQuali.split("\\|");
			for (String dignity : dignities) {
				if (mandatorDignityCodes.contains(dignity.trim())) {
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList());
	}

	/**
	 * Get the current state of the dignity filtering flag.
	 * 
	 * @return true if filtering by dignity is enabled, false otherwise
	 */
	public boolean isOnlyValidForDignity() {
		return bOnlyValidForDignity;
	}

	/**
	 * Set whether to filter services by dignity. When true, only services where
	 * DigniQuali includes at least one of the current mandator's dignities are
	 * returned.
	 * 
	 * @param onlyValidForDignity true to enable dignity filtering, false to disable
	 */
	public void setOnlyValidForDignity(boolean onlyValidForDignity) {
		this.bOnlyValidForDignity = onlyValidForDignity;
	}

	/**
	 * Check if the model service is available
	 */
	public boolean isServiceAvailable() {
		return isServiceAvailable(null);
	}

	/**
	 * Check if the model service is available
	 * 
	 * @param context Optional BundleContext
	 */
	public boolean isServiceAvailable(BundleContext context) {
		return getModelService(context) != null;
	}

	/**
	 * Get a specific ITardocLeistung by its code.
	 * 
	 * @param code    The Tardoc code (e.g., "CA.00.0010")
	 * @param context Optional BundleContext
	 * @return The ITardocLeistung if found, null otherwise
	 */
	public ITardocLeistung getLeistungByCode(String code, BundleContext context) {
		IModelService service = getModelService(context);
		if (service == null) {
			System.err.println("TardocManager: IModelService is not available");
			return null;
		}

		try {
			IQuery<ITardocLeistung> query = service.getQuery(ITardocLeistung.class);
			query.and("code_", COMPARATOR.EQUALS, code);
			query.and("deleted", COMPARATOR.EQUALS, false);

			List<ITardocLeistung> results = query.execute();
			if (results != null && !results.isEmpty()) {
				return results.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Check if the current mandator has a specific dignity code.
	 * 
	 * @param dignityCode The dignity code to check (e.g., "3010")
	 * @return true if the mandator has this dignity, false otherwise
	 */
	public boolean mandatorHasDignity(String dignityCode) {
		IMandator mandator = ContextServiceHolder.get().getActiveMandator().orElse(null);
		if (mandator == null) {
			return false;
		}

		List<ICoding> mandatorDignities = ArzttarifeUtil.getMandantTardocSepcialist(mandator);
		if (mandatorDignities == null || mandatorDignities.isEmpty()) {
			return false;
		}

		return mandatorDignities.stream().anyMatch(coding -> dignityCode.equals(coding.getCode()));
	}
}
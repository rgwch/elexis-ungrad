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

import java.time.LocalDate;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IEncounter;
import ch.elexis.data.Konsultation;
import ch.elexis.ungrad.tardoc.views.TardocKonsView;

public class BillingsManager {
	private IEncounter kons;
	private Konsultation b;
	private TardocKonsView tkv;

	public BillingsManager(TardocKonsView view) {
		this.tkv=view;
	}

	public void setEncounter(IEncounter kons) {
		this.kons = kons;
		this.b = Konsultation.load(kons.getId());

	}
	
	public boolean isToday() {
		LocalDate konsDate = kons.getDate();
		return konsDate.isEqual(LocalDate.now());
	}
	
	/**
	 * Check conditions and automatically bill CA.00.0010 if:
	 * - Current mandator has dignity 3010
	 * - Current encounter has no billings
	 * - Timer is being started (not resumed)
	 */
	public void autostart() {
		if (this.kons == null) {
			return;
		}
		
		// Check if there are already billings
		List<ch.elexis.core.model.IBilled> billed = this.kons.getBilled();
		if (billed != null && !billed.isEmpty()) {
			// Already has billings, don't auto-add
			return;
		}
		
		// Get TardocManager
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		TardocManager manager = TardocManagerHolder.get();
		if (manager == null) {
			manager = new TardocManager();
		}
		
		// Check if mandator has dignity 3010
		if (!manager.mandatorHasDignity("3010")) {
			return;
		}
		
		// Get CA00.0010
		ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung consultation = 
			manager.getLeistungByCode("CA.00.0010", bundleContext);
		
		if (consultation != null) {
			// Add to billing
			ch.elexis.core.services.holder.BillingServiceHolder.get()
				.bill(consultation, this.kons, 1);
			
			// Trigger update event to refresh the display
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, this.kons);
		}

	}
}

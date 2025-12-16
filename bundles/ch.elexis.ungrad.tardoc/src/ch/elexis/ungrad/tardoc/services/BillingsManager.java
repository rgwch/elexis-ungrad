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
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.ungrad.tardoc.views.TardocKonsView;
import ch.rgw.tools.VersionedResource;

public class BillingsManager {
	private IEncounter kons;
	private TardocKonsView tkv;
	private TardocConfig config;
	private TardocConfig.DignityConfig activeDignityConfig;
	private String activeDignityCode;

	private int autoBillingCount = 0;
	private long lastBilledMinute = -1;

	BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private TardocManager manager = TardocManager.getInstance();

	public BillingsManager(TardocKonsView view) {
		this.tkv = view;

		// read rsc/config.json and parse it with gson
		this.config = TardocConfig.load();
		// Determine the active dignity configuration
		updateActiveDignityConfig();
	}

	/**
	 * Update the active dignity configuration based on the current mandator's
	 * dignities
	 */
	private void updateActiveDignityConfig() {
		if (config == null || config.getDignityConfigs() == null) {
			return;
		}

		// Check all configured dignities to find one that matches the current mandator
		for (String dignityCode : config.getDignityConfigs().keySet()) {
			if (manager.mandatorHasDignity(dignityCode)) {
				this.activeDignityConfig = config.getDignityConfig(dignityCode);
				this.activeDignityCode = dignityCode;
				System.out.println("BillingsManager: Active dignity " + dignityCode + ", autobilling "
						+ (activeDignityConfig.isAutobillingEnabled() ? "enabled" : "disabled"));
				break;
			}
		}
	}

	/**
	 * Get the loaded configuration
	 * 
	 * @return the TardocConfig instance or null if loading failed
	 */
	public TardocConfig getConfig() {
		return config;
	}

	/**
	 * Get the active dignity configuration for the current mandator
	 * 
	 * @return the DignityConfig instance or null if no matching dignity found
	 */
	public TardocConfig.DignityConfig getActiveDignityConfig() {
		return activeDignityConfig;
	}

	/**
	 * Get the active dignity code for the current mandator
	 * 
	 * @return the dignity code (e.g. "3010") or null if no matching dignity found
	 */
	public String getActiveDignityCode() {
		return activeDignityCode;
	}

	public void setEncounter(IEncounter kons) {
		this.kons = kons;

	}

	public boolean isToday() {
		LocalDate konsDate = kons.getDate();
		return konsDate.isEqual(LocalDate.now());
	}

	/**
	 * Check conditions and automatically bill if: - Current mandator has one of the
	 * dignities configured in TardocConfig - Autobilling is enabled for that
	 * dignity
	 */
	public void autobill(long minutesElapsed) {
		if (this.kons == null) {
			return;
		}

		// Check if we have an active dignity configuration
		if (this.activeDignityConfig == null) {
			System.out.println("BillingsManager: No active dignity configuration, skipping autostart");
			return;
		}

		// Check if autobilling is enabled for this dignity
		if (!activeDignityConfig.isAutobillingEnabled()) {
			System.out.println("BillingsManager: Autobilling disabled for dignity " + activeDignityCode);
			return;
		}

		// Check if there are already billings
		List<ch.elexis.core.model.IBilled> billed = this.kons.getBilled();
		if (billed == null || billed.isEmpty()) {

			// Billings are empty. Get the initial billing code
			String initialBillingCode = activeDignityConfig.getInitialBilling();
			if (initialBillingCode == null || initialBillingCode.trim().isEmpty()) {
				System.err.println(
						"BillingsManager: No initial billing code configured for dignity " + activeDignityCode);
				return;
			}

			// Get the ITardocLeistung for the initial billing code
			ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung initialBilling = manager
					.getLeistungByCode(initialBillingCode, bundleContext);

			if (initialBilling != null) {
				// Add to billing
				ch.elexis.core.services.holder.BillingServiceHolder.get().bill(initialBilling, this.kons, 1);

				autoBillingCount = 1;
				lastBilledMinute = (int) minutesElapsed;

				System.out.println("BillingsManager: Applied initial billing " + initialBillingCode + " for dignity "
						+ activeDignityCode);

				// Trigger update event to refresh the display
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, this.kons);

			} else {
				System.err.println(
						"BillingsManager: Initial billing code " + initialBillingCode + " not found in Tardoc");
			}
			return;
		} else {
			// There are existing billings. Check if any were added manually
			int numBilled = 0;
			for (IBilled ib : billed) {
				if (!ib.isDeleted()) {
					numBilled += ib.getAmount();
					String text = getStopTimerText(ib);
					if (text != null) {
						this.tkv.insertTextAtEndOfKons(text);
						this.stopAutoBilling();
						return;
					}
				}
			}
			if (this.autoBillingCount < numBilled) {
				System.out.println("BillingsManager: Manual billing detected, skipping autobill");
				this.stopAutoBilling();
				return;
			}
			// No manual billings detected, proceed with automatic followup billing
			TardocConfig.FollowupBilling followup = activeDignityConfig.getFollowupBilling();
			if (followup.getCode() == null || followup.getCode().trim().isEmpty()) {
				return;
			}
			int afterMinutes = followup.getAfter();
			int everyMinutes = followup.getEvery();
			int maxCount = followup.getMax();

			// Check if we've reached the "after" threshold
			if (minutesElapsed >= afterMinutes && autoBillingCount < (maxCount + 1)) {
				// Check if we should bill (every N minutes after the initial threshold)
				long minutesSinceThreshold = minutesElapsed - afterMinutes;

				// Only bill if we're at the right interval AND we haven't billed in this minute
				long currentMinute = minutesElapsed;
				if (minutesSinceThreshold % everyMinutes == 0 && lastBilledMinute != currentMinute) {
					lastBilledMinute = currentMinute;

					// Apply followup billing

					ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung followupBilling = manager
							.getLeistungByCode(followup.getCode(), bundleContext);

					if (followupBilling == null) {
						System.err.println("BillingsManager: Followup billing code " + followup.getCode()
								+ " not found in Tardoc");
						return;
					}
					ch.elexis.core.services.holder.BillingServiceHolder.get().bill(followupBilling, this.kons, 1);
					autoBillingCount++;
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, this.kons);

					// Check if we've reached max
					if (autoBillingCount >= (maxCount + 1)) {
						System.out.println(
								"BillingsManager: Reached max followup billings (" + maxCount + "), stopping timer");
						this.stopAutoBilling();
					}
				}
			}

		}

	}

	private void stopAutoBilling() {
		this.tkv.encounterTimer.pause();
	}

	

	/**
	 * Get the stop_timer text for a given billed item.
	 * 
	 * @param billed the billed item
	 * @return the default text from stop_timer map, or null if not found
	 */
	public String getStopTimerText(IBilled billed) {
		if (billed == null || activeDignityConfig == null) {
			return null;
		}

		Map<String, String> stopTimer = activeDignityConfig.getStopTimer();
		if (stopTimer == null || stopTimer.isEmpty()) {
			return null;
		}

		// Get the code from the billed item
		String billedCode = billed.getCode();
		if (billedCode == null) {
			return null;
		}

		// Look up in stop_timer map
		return stopTimer.get(billedCode);
	}

}
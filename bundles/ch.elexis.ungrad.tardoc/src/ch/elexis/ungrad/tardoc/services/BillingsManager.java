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
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IEncounter;
import ch.elexis.data.Konsultation;
import ch.elexis.ungrad.tardoc.views.TardocKonsView;

public class BillingsManager {
	private IEncounter kons;
	private Konsultation b;
	private TardocKonsView tkv;
	private TardocConfig config;
	private TardocConfig.DignityConfig activeDignityConfig;
	private String activeDignityCode;

	// Timer monitoring for followup billing
	private Timer followupTimer;
	private int followupBillingCount = 0;
	private int initialBillingCount = 0;
	private boolean timerStopped = false;

	// Event handler for billing changes
	private EventHandler billingEventHandler;

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

		TardocManager manager = TardocManagerHolder.get();
		if (manager == null) {
			manager = new TardocManager();
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
		this.b = Konsultation.load(kons.getId());

	}

	public boolean isToday() {
		LocalDate konsDate = kons.getDate();
		return konsDate.isEqual(LocalDate.now());
	}

	/**
	 * Check conditions and automatically bill the initial billing code if: -
	 * Current mandator has one of the dignities configured in TardocConfig -
	 * Autobilling is enabled for that dignity - Current encounter has no billings -
	 * Timer is being started (not resumed)
	 */
	public void autostart() {
		if (this.kons == null) {
			return;
		}

		// Check if we have an active dignity configuration
		if (this.activeDignityConfig == null) {
			System.out.println("BillingsManager: No active dignity configuration, skipping autostart");
			return;
		}

		// Check if there are already billings
		List<ch.elexis.core.model.IBilled> billed = this.kons.getBilled();
		if (billed != null && !billed.isEmpty()) {
			// Already has billings, don't auto-add
			return;
		}

		// Check if autobilling is enabled for this dignity
		if (!activeDignityConfig.isAutobillingEnabled()) {
			System.out.println("BillingsManager: Autobilling disabled for dignity " + activeDignityCode);
			return;
		}

		// Get the initial billing code
		String initialBillingCode = activeDignityConfig.getInitialBilling();
		if (initialBillingCode == null || initialBillingCode.trim().isEmpty()) {
			System.err.println("BillingsManager: No initial billing code configured for dignity " + activeDignityCode);
			return;
		}

		// Get TardocManager
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		TardocManager manager = TardocManagerHolder.get();
		if (manager == null) {
			manager = new TardocManager();
		}

		// Get the ITardocLeistung for the initial billing code
		ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung initialBilling = manager
				.getLeistungByCode(initialBillingCode, bundleContext);

		if (initialBilling != null) {
			// Add to billing
			ch.elexis.core.services.holder.BillingServiceHolder.get().bill(initialBilling, this.kons, 1);

			initialBillingCount++;

			System.out.println("BillingsManager: Applied initial billing " + initialBillingCode + " for dignity "
					+ activeDignityCode);

			// Trigger update event to refresh the display
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, this.kons);

			// Start followup billing timer if configured
			startFollowupTimer();
		} else {
			System.err.println("BillingsManager: Initial billing code " + initialBillingCode + " not found in Tardoc");
		}
	}

	/**
	 * Start the followup billing timer if followup billing is configured
	 */
	private void startFollowupTimer() {
		// Check if followup billing is configured
		if (activeDignityConfig == null || activeDignityConfig.getFollowupBilling() == null) {
			return;
		}

		TardocConfig.FollowupBilling followup = activeDignityConfig.getFollowupBilling();
		if (followup.getCode() == null || followup.getCode().trim().isEmpty()) {
			return;
		}

		// Cancel existing timer if any
		stopFollowupTimer();

		// Reset counter and flag
		followupBillingCount = 0;
		timerStopped = false;

		int afterMinutes = followup.getAfter();
		int everyMinutes = followup.getEvery();
		int maxCount = followup.getMax();

		System.out.println("BillingsManager: Starting followup timer - after " + afterMinutes + " min, every "
				+ everyMinutes + " min, max " + maxCount + " times");

		// Create timer
		followupTimer = new Timer("FollowupBillingTimer", true);

		// Schedule task to check and apply followup billing
		followupTimer.scheduleAtFixedRate(new TimerTask() {
			private long startTime = System.currentTimeMillis();
			private long lastBillingTime = -1;

			@Override
			public void run() {
				try {
					// Check if timer has been stopped
					if (timerStopped || followupTimer == null) {
						cancel();
						return;
					}

					// Check for manual billing FIRST before any other logic
					if (checkForManualBilling()) {
						System.out.println("BillingsManager: Manual billing detected, stopping followup timer");
						stopFollowupTimer();
						cancel();
						return;
					}

					long elapsedMinutes = (System.currentTimeMillis() - startTime) / (60 * 1000);

					// Check if we've reached the "after" threshold
					if (elapsedMinutes >= afterMinutes && followupBillingCount < maxCount) {
						// Check if we should bill (every N minutes after the initial threshold)
						long minutesSinceThreshold = elapsedMinutes - afterMinutes;

						// Only bill if we're at the right interval AND we haven't billed in this minute
						long currentMinute = elapsedMinutes;
						if (minutesSinceThreshold % everyMinutes == 0 && lastBillingTime != currentMinute) {
							lastBillingTime = currentMinute;

							// Apply followup billing
							applyFollowupBilling();

							// Check if we've reached max
							if (followupBillingCount >= maxCount) {
								System.out.println("BillingsManager: Reached max followup billings (" + maxCount
										+ "), stopping timer");
								stopFollowupTimer();
								cancel();
							}
						}
					}
				} catch (Exception e) {
					System.err.println("BillingsManager: Error in followup timer: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}, 60000, 60000); // Check every minute
	}

	/**
	 * Stop the followup billing timer
	 */
	private synchronized void stopFollowupTimer() {
		// Set flag FIRST to stop the timer task from running
		timerStopped = true;

		if (followupTimer != null) {
			Timer timerToStop = followupTimer;
			followupTimer = null; // Clear reference immediately

			try {
				timerToStop.cancel();
				timerToStop.purge(); // Remove cancelled tasks
				System.out.println("BillingsManager: Followup timer stopped and cancelled");
			} catch (Exception e) {
				System.err.println("BillingsManager: Error stopping timer: " + e.getMessage());
			}
		} else {
			System.out.println("BillingsManager: Followup timer already stopped");
		}
	}

	/**
	 * Check if user has manually added billings (which should stop the timer)
	 * 
	 * @return true if manual billing detected
	 */
	private boolean checkForManualBilling() {
		if (kons == null) {
			return false;
		}

		List<ch.elexis.core.model.IBilled> billed = kons.getBilled();
		if (billed == null) {
			return false;
		}

		// Count billings we've applied vs total billings
		int expectedCount = initialBillingCount + followupBillingCount;
		int actualCount = billed.size();

		// Debug logging
		if (actualCount != expectedCount) {
			System.out.println("BillingsManager: Billing count mismatch - Expected: " + expectedCount + ", Actual: "
					+ actualCount + " (initial=" + initialBillingCount + ", followup=" + followupBillingCount + ")");
		}

		// If there are more billings than we've applied, assume manual billing
		if (actualCount > expectedCount) {
			System.out.println("BillingsManager: Manual billing detected! Expected " + expectedCount + " but found "
					+ actualCount + " billings");
			return true;
		}

		return false;
	}

	/**
	 * Apply the followup billing code
	 */
	private void applyFollowupBilling() {
		if (activeDignityConfig == null || activeDignityConfig.getFollowupBilling() == null) {
			return;
		}

		String followupCode = activeDignityConfig.getFollowupBilling().getCode();
		if (followupCode == null || followupCode.trim().isEmpty()) {
			return;
		}

		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		TardocManager manager = TardocManagerHolder.get();
		if (manager == null) {
			manager = new TardocManager();
		}

		ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung followupBilling = manager.getLeistungByCode(followupCode,
				bundleContext);

		if (followupBilling != null) {
			ch.elexis.core.services.holder.BillingServiceHolder.get().bill(followupBilling, this.kons, 1);

			followupBillingCount++;

			System.out.println(
					"BillingsManager: Applied followup billing " + followupCode + " (#" + followupBillingCount + ")");

			// Trigger update event to refresh the display
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, this.kons);
		} else {
			System.err.println("BillingsManager: Followup billing code " + followupCode + " not found in Tardoc");
		}
	}

	/**
	 * Stop the timer (called when user manually adds billing or closes the
	 * encounter)
	 */
	public void stopTimer() {
		System.out.println("BillingsManager: Stopping timer (called externally)");
		stopFollowupTimer();
	}

	/**
	 * Notify that a billing was manually added - this will stop the followup timer
	 */
	public void onBillingManuallyAdded() {
		System.out.println("BillingsManager: Manual billing added notification received, stopping followup timer");
		stopFollowupTimer();
	}

	/**
	 * Manually check for billing changes and stop timer if manual billing detected.
	 * This should be called from the UI when billings are modified.
	 */
	public void checkBillingChanges() {
		if (!isFollowupTimerRunning()) {
			return; // Timer not running, nothing to check
		}

		if (checkForManualBilling()) {
			System.out.println("BillingsManager: Manual billing detected via checkBillingChanges()");
			stopFollowupTimer();
		}
	}

	/**
	 * Check if the followup timer is currently running
	 */
	public boolean isFollowupTimerRunning() {
		return followupTimer != null && !timerStopped;
	}

	/**
	 * Get followup billing count
	 */
	public int getFollowupBillingCount() {
		return followupBillingCount;
	}
}

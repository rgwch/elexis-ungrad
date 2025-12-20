/*******************************************************************************
 * Copyright (c) 2025 by G. Weirich
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * G. Weirich - initial implementation
 * Substantial contributions:  Copilot (c) 2024 GitHub, Inc. using Claude Sonnet 4.5
 *********************************************************************************/

package ch.elexis.ungrad.tardoc.services;

/**
 * Example usage of TardocConfig
 * 
 * This class demonstrates how to load and use the configuration from rsc/config.json
 */
public class TardocConfigExample {
	
	public static void exampleUsage() {
		// Load the configuration
		TardocConfig config = TardocConfig.load();
		
		if (config != null) {
			// Get configuration for dignity code "3010"
			TardocConfig.DignityConfig dignityConfig = config.getDignityConfig("3010");
			
			if (dignityConfig != null) {
				// Check if autobilling is enabled
				boolean autoEnabled = dignityConfig.isAutobillingEnabled();
				System.out.println("Autobilling enabled: " + autoEnabled);
				
				// Get initial billing code
				String initialBilling = dignityConfig.getInitialBilling();
				System.out.println("Initial billing code: " + initialBilling);
				
				// Get followup billing information
				TardocConfig.FollowupBilling followup = dignityConfig.getFollowupBilling();
				if (followup != null) {
					System.out.println("Followup code: " + followup.getCode());
					System.out.println("After: " + followup.getAfter() + " days");
					System.out.println("Every: " + followup.getEvery() + " days");
				}
				
				// Get stop timer codes and their default text
				if (dignityConfig.getStopTimer() != null) {
					dignityConfig.getStopTimer().forEach((code, text) -> {
						System.out.println("Stop timer code " + code + ": " + text);
					});
				}
				
				// Get continue timer codes and their default text
				if (dignityConfig.getContinueTimer() != null) {
					dignityConfig.getContinueTimer().forEach((code, text) -> {
						System.out.println("Continue timer code " + code + ": " + text);
					});
				}
			}
		}
	}
}

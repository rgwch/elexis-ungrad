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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Configuration model for Tardoc billing rules
 */
public class TardocConfig {
	
	/**
	 * Configuration for a specific dignity code
	 */
	public static class DignityConfig {
		public boolean autobilling_enabled;
		public String initial_billing;
		public FollowupBilling followup_billing;
		public Map<String, String> stop_timer;
		public Map<String, String> continue_timer;
		
		public boolean isAutobillingEnabled() {
			return autobilling_enabled;
		}
		
		public String getInitialBilling() {
			return initial_billing;
		}
		
		public FollowupBilling getFollowupBilling() {
			return followup_billing;
		}
		
		public Map<String, String> getStopTimer() {
			return stop_timer;
		}
		
		public Map<String, String> getContinueTimer() {
			return continue_timer;
		}
	}
	
	/**
	 * Configuration for followup billing
	 */
	public static class FollowupBilling {
		public String code;
		public String after;
		public String every;
		public String max;
		
		public String getCode() {
			return code;
		}
		
		public int getMax() {
			try {
				return Integer.parseInt(max);
			} catch (NumberFormatException e) {
				return 1;
			}
		}
		public int getAfter() {
			try {
				return Integer.parseInt(after);
			} catch (NumberFormatException e) {
				return 1;
			}
		}
		
		public int getEvery() {
			try {
				return Integer.parseInt(every);
			} catch (NumberFormatException e) {
				return 1;
			}
		}
	}
	
	// Map of dignity codes to their configurations
	private Map<String, DignityConfig> dignityConfigs;
	
	/**
	 * Get configuration for a specific dignity code
	 * @param dignityCode the dignity code (e.g. "3010")
	 * @return the dignity configuration or null if not found
	 */
	public DignityConfig getDignityConfig(String dignityCode) {
		return dignityConfigs != null ? dignityConfigs.get(dignityCode) : null;
	}
	
	/**
	 * Get all dignity configs
	 */
	public Map<String, DignityConfig> getDignityConfigs() {
		return dignityConfigs;
	}
	
	/**
	 * Set dignity configs (used by Gson during deserialization)
	 */
	public void setDignityConfigs(Map<String, DignityConfig> configs) {
		this.dignityConfigs = configs;
	}
	
	/**
	 * Custom Gson deserializer for TardocConfig
	 */
	private static class TardocConfigDeserializer implements JsonDeserializer<TardocConfig> {
		@Override
		public TardocConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			TardocConfig config = new TardocConfig();
			JsonObject jsonObject = json.getAsJsonObject();
			Map<String, DignityConfig> dignityConfigs = new HashMap<>();
			
			// Iterate through all entries in the JSON
			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				String key = entry.getKey();
				JsonElement value = entry.getValue();
				
				// Assume any numeric key is a dignity code
				try {
					Integer.parseInt(key);
					DignityConfig dignityConfig = context.deserialize(value, DignityConfig.class);
					dignityConfigs.put(key, dignityConfig);
				} catch (NumberFormatException e) {
					// Not a dignity code, skip
				}
			}
			
			config.setDignityConfigs(dignityConfigs);
			return config;
		}
	}
	
	/**
	 * Load configuration from rsc/config.json
	 * @return the loaded TardocConfig or null if loading failed
	 */
	public static TardocConfig load() {
		try {
			Bundle bundle = FrameworkUtil.getBundle(TardocConfig.class);
			if (bundle != null) {
				InputStream is = bundle.getResource("rsc/config.json").openStream();
				Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
				
				Gson gson = new GsonBuilder()
					.registerTypeAdapter(TardocConfig.class, new TardocConfigDeserializer())
					.create();
				
				TardocConfig config = gson.fromJson(reader, TardocConfig.class);
				reader.close();
				return config;
			}
		} catch (Exception e) {
			System.err.println("Error loading config.json: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}

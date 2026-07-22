/*******************************************************************************
 * Copyright (c) 2024-2026 by G. Weirich
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

package ch.elexis.ungrad;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.io.Settings;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;


public class AIUtil {
	static Config cfg=Config.getDefaultInstance();
	public static boolean useAI() {
		boolean bUseAI=cfg.getBoolean(PreferenceConstants.USE_AI);
		return bUseAI;
	}
	
	public static String sendPrompt(String model, String prompt) {
		String requestBody = String.format("{ \"stream\":false, \"model\": \"%s\", \"prompt\": \"%s\" }",
				model, prompt);
		try {
			Http http=new Http();
			URI uri = new URI(cfg.getString(PreferenceConstants.AI_URL));
			String result = http.doPost(uri.toURL(), requestBody, 200);
			if (!StringTool.isNothing(result)) {
				Gson gson = new Gson();	

			    Type type = new TypeToken<Map<String, String>>() {}.getType();
			    Map<String, String> json = gson.fromJson(result, type);

			    String response = json.get("response");
			    return response;	}
		} catch (Exception e) {
			ExHandler.handle(e);
		}
	
		return "";
	}
}

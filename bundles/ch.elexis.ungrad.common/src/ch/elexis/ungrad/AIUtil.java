package ch.elexis.ungrad;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;


public class AIUtil {
	public static boolean useAI() {
		boolean bUseAI=CoreHub.localCfg.get(PreferenceConstants.USE_AI, false);
		return bUseAI;
	}
	
	public static String sendPrompt(String model, String prompt) {
		String requestBody = String.format("{ \"stream\":false, \"model\": \"%s\", \"prompt\": \"%s\" }",
				model, prompt);
		try {
			Http http=new Http();
			URI uri = new URI(CoreHub.localCfg.get(PreferenceConstants.AI_URL, ""));
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

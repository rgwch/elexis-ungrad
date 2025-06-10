package ch.elexis.ungrad;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

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
			URL url = new URL(CoreHub.localCfg.get(PreferenceConstants.AI_URL, ""));
			String result = http.doPost(url, requestBody, 200);
			if (!StringTool.isNothing(result)) {
				ObjectMapper mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String, String> json = mapper.readValue(result.getBytes(),
						HashMap.class);
				String response = json.get("response");
				return response;
			}
		} catch (Exception e) {
			ExHandler.handle(e);
		}
	
		return "";
	}
}

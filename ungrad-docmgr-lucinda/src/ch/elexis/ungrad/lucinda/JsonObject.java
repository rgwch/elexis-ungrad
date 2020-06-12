package ch.elexis.ungrad.lucinda;

import java.util.HashMap;
import java.util.Map;

public class JsonObject {
	Map<String,String> cnt=new HashMap<>();
	
	public void put(String key, String value) {
		cnt.put(key, value);
	}
	public String get(String key) {
		return cnt.get(key);
	}
}

package ch.elexis.ungrad.lucinda;

import java.util.HashMap;
import java.util.Map;

public class JsonObject {
	Map<String,Object> cnt=new HashMap<>();
	
	public void put(String key, String value) {
		cnt.put(key, value);
	}
	public String getString(String key) {
		return (String)cnt.get(key);
	}
	public void put(String key, byte[] data) {
		cnt.put(key, data);
	}
}

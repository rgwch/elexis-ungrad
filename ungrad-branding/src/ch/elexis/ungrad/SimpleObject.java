package ch.elexis.ungrad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import ch.elexis.core.ui.util.Log;

public abstract class SimpleObject {
	public abstract String[] getFields();

	protected Map<String, String> props = new HashMap<>();
	protected Log log = Log.get(getClass().getName());

	protected void load(ResultSet res) {
		for (String field : getFields()) {
			try {
				props.put(field.toLowerCase(), res.getString(field));
			} catch (SQLException e) {
				log.log("Illegal field name " + field, Log.FATALS);
			}
		}

	}
	
	public String get(String field) {
		for (String f : getFields()) {
			if (f.equalsIgnoreCase(field)) {
				return props.get(field.toLowerCase());
			}
		}
		throw new Error("Internal error: Bad field requested " + field);
	}

	public void set(String field, String value){
		props.put(field.toLowerCase(),value);
	}
	protected int compare(SimpleObject other, String field){
		if(get(field)==null){
			if(other.get(field)==null){
				return 0;
			}else{
				return 2;
			}
		}else{
			if(other.get(field)==null){
				return -1;
			}else{
				return get(field).compareTo(other.get(field));
			}
		}
	}
}

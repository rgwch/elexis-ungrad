package ch.elexis.ungrad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.StringTool;


public abstract class SimpleObject {
	public abstract String[] getFields();

	protected Map<String, String> props = new HashMap<>();
	protected  Logger log = LoggerFactory.getLogger(getClass().getName());

	protected void load(ResultSet res) {
		for (String field : getFields()) {
			try {
				props.put(field.toLowerCase(), res.getString(field));
			} catch (SQLException e) {
				log.error("Illegal field name " + field);
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

	public void set(String field, String value) {
		props.put(field.toLowerCase(), value);
	}

	protected int compare(SimpleObject other, String field) {
		if (get(field) == null) {
			if (other.get(field) == null) {
				return 0;
			} else {
				return 2;
			}
		} else {
			if (other.get(field) == null) {
				return -1;
			} else {
				String cnt = get(field);
				String o = other.get(field);
				if (cnt.matches("[\\d\\., ]+")) {
					Float f1 = makeFloat(cnt);
					Float f2 = makeFloat(o);
					return f1.compareTo(f2);
				} else {
					return cnt.compareTo(o);
				}
			}
		}
	}

	/**
	 * Make a positive float from a string. We don't use just
	 * Float.parseFloat(), because we want to handle:
	 * <ul>
	 * <li>, or . as dezimal separator</li>
	 * <li>leading &lt; or &gt; symbols</li>
	 * <li>whitespace</li>
	 * </ul>
	 * 
	 * @param s
	 *            the String to parse
	 * @return the float (which is 0.0 or higher) or -1f if the String could not
	 *         be parsed
	 */
	public static float makeFloat(String raw) {
		String s = raw.replaceAll("[\\s]", "");
		if (s.startsWith("<")) {
			return makeFloatInternal(s.substring(1));
		} else if (s.startsWith(">")) {
			return makeFloatInternal(s.substring(1));
		} else {
			return makeFloatInternal(s);
		}
	}

	private static float makeFloatInternal(String s) {
		String[] splitted = s.split("[\\.,]");
		if (splitted[0].matches("\\s*[0-9]+\\s*")) {
			String einer = splitted[0].trim();
			String frac = "0";
			if (splitted.length > 2) {
				return -1.0f;
			}
			if (splitted.length == 2) {
				if (splitted[1].matches("\\s*[0-9]+\\s*")) {
					frac = splitted[1].trim();
				} else {
					return -1.0f;
				}
			}
			frac = StringTool.pad(StringTool.RIGHTS, '0', frac, 3);
			float fr = ((float) Integer.parseInt(frac)) / 1000f;
			float ret = (float) Integer.parseInt(einer) + fr;
			return ret;
		} else {
			return -1.0f;
		}
	}
}

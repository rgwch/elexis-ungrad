package ch.elexis.ungrad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.data.Person;

public class Util {
	static Logger log = LoggerFactory.getLogger("elexis ungrad Util");
	static final String msg = "Requirement failed: ";
	
	public static void require(boolean it, String desc){
		if (it == false) {
			log.error(msg + desc);
			throw new Error(msg + desc);
		}
	}
	
	public static final boolean isFemale(Person p){
		if (p.getGeschlecht().equalsIgnoreCase("m")) {
			return false;
		}
		return true;
	}
}

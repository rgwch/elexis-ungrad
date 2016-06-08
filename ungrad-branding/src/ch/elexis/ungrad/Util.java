package ch.elexis.ungrad;

import ch.elexis.core.ui.util.Log;

public class Util {
	static Log log=Log.get("elexis ungrad Util");
	static final String msg="Requirement failed: ";
	
	public static void require(boolean it, String desc){
		if(it==false){
			log.log(msg+desc, Log.DEBUGMSG);
			throw new Error(msg+desc);
		}
	}
}

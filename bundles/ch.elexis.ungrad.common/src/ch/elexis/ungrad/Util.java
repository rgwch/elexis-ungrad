/*******************************************************************************
 * Copyright (c) 2016-2024, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.data.Person;

public class Util {
	static Logger log = LoggerFactory.getLogger("elexis ungrad Util");
	static final String msg = "Requirement failed: ";

	public static void require(boolean it, String desc) {
		if (it == false) {
			log.error(msg + desc);
			throw new Error(msg + desc);
		}
	}

	public static final boolean isFemale(Person p) {
		if (p.getGeschlecht().equalsIgnoreCase("m")) {
			return false;
		}
		return true;
	}

	public static final String reduceCharset(String in) {
		String out = in.replace("ü", "ue").replace("ä", "ae").replace("ö", "oe").replaceAll("[éè]", "e").replace("à",
				"a");
		return out;
	}
}

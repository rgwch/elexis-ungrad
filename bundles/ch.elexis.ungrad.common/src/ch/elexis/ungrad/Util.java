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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.types.Gender;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.rgw.tools.StringTool;

public class Util {
	static Logger log = LoggerFactory.getLogger("elexis ungrad Util");
	static final String msg = "Requirement failed: ";

	public static void require(boolean it, String desc) {
		if (it == false) {
			log.error(msg + desc);
			throw new Error(msg + desc);
		}
	}

	/**
	 * Shortcut to check wheter a Person is female (dependig on Elexis version, the GENDER field is w, f or Gender.FEMALE)
	 * So we just check if the Gender is male and assume, political incorrectly, female gender, if not,
	 * @param p
	 * @return true if the person is not male.
	 */
	public static final boolean isFemale(IPerson p) {
		if (p.getGender().equals(Gender.MALE)) {
			return false;
		}
		return true;
	}

	/**
	 * replace characters which are in some circumstances problematic with safe ones.  
	 * @param in
	 * @return
	 */
	public static final String reduceCharset(String in) {
		String out = in.replace("ü", "ue").replace("ä", "ae").replace("ö", "oe").replaceAll("[éè]", "e").replace("à",
				"a");
		return out;
	}

	/**
	 * Try to find the social insurance number. If it's set as XID, we're lucky. Else check ICoverages, if the insurance number could be the SSN
	 * (Which is totally possible and legal)
	 * @param pat
	 * @return a String which is possible empty, but not null
	 */
	public static final String getSSN(IPatient pat) {
		String ahv = "";
		try {
			ahv = pat.getXid(XidConstants.CH_AHV).getDomainId();
		} catch (Exception ex) {
			// nothing
		}
		if (StringTool.isNothing(ahv)) {
			List<ICoverage> coverages = pat.getCoverages();
			for (ICoverage fall : coverages) {
				String nr = fall.getInsuranceNumber();
				if (!StringTool.isNothing(nr) && nr.matches("\\b756\\.?[0-9]{4}\\.?[0-9]{4}\\.?[0-9]{2}\\b")) {
					ahv = fall.getInsuranceNumber();
					break;
				}
			}
		}
		return PersistentObject.checkNull(ahv);
	}
}

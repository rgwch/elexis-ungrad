/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/
package ch.elexis.ungrad.labview.model;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.Util;
import ch.rgw.tools.StringTool;

/**
 * A Bucket is a collection of all LabResults for a given LabItem and a given
 * Patient. It can decide, whether a result is pathologic, and it can perform
 * some statistics.
 * 
 * @author gerry
 *
 */
public class Bucket {
	final String ntyp = Integer.toString(LabItemTyp.NUMERIC.ordinal());

	Item item;
	Patient pat;
	List<Result> results = new ArrayList<Result>();
	static final int LOWER_BOUND_MALE = 0;
	static final int UPPER_BOUND_MALE = 1;
	static final int LOWER_BOUND_FEMALE = 2;
	static final int UPPER_BOUND_FEMALE = 3;

	float[] refBounds = new float[4];

	Bucket(Patient patient, Item item) {
		Util.require(item!=null, "Bucket: item must not be null");
		Util.require(patient!=null, "Bucket: Patient must not be null");
		this.item = item;
		pat = patient;
		if(item.get("typ")==null){
			System.out.println("item "+item.get("titel"));
		}
		if (item.get("typ").equals(ntyp)) {
			makeBounds(item.get("refMann"), 0);
			makeBounds(item.get("refFrauOrTx"), 2);
		}
	}

	private void makeBounds(String val, int index) {
		if (!StringTool.isNothing(val)) {
			String chopped = val.replaceAll("\\s", "");
			if (chopped.startsWith("<")) {
				refBounds[index] = 0;
				refBounds[index + 1] = makeFloat(chopped.substring(1));
			} else if (chopped.startsWith(">")) {
				refBounds[index] = makeFloat(chopped.substring(1));
				refBounds[index + 1] = Float.MAX_VALUE;
			} else {
				String[] bounds = chopped.split("-");
				if (bounds.length > 0) {
					refBounds[index] = makeFloat(bounds[0]);
					if (bounds.length == 2) {
						refBounds[index + 1] = makeFloat(bounds[1]);
					}
				}
			}
		}
	}

	/**
	 * Add a Result to the bucket
	 * 
	 * @param result
	 *            The Result to add
	 */
	void addResult(Result result) {
		results.add(result);
	}

	/**
	 * Get the number of Results in the Bucket.
	 * 
	 * @return a positive Integer
	 */
	public int getResultCount() {
		return results.size();
	}

	/**
	 * return the numerically lowest Result in the Bucket
	 * 
	 * @return a stringified version of the lowest Result
	 */
	public String getMinResult() {
		float cmp = Float.MAX_VALUE;
		for (Result result : results) {
			float val = makeFloat(result.get("resultat"));
			if (val < cmp) {
				cmp = val;
			}
		}
		return Float.toString(cmp);
	}

	/**
	 * Get the numerically highest Result in the Bucket
	 * 
	 * @return a stringified versiuon of the highest Result.
	 */
	public String getMaxResult() {
		float cmp = 0;
		for (Result result : results) {
			float val = makeFloat(result.get("resultat"));
			if (val > cmp) {
				cmp = val;
			}
		}
		return Float.toString(cmp);
	}

	/**
	 * Get the median value of all Results in the bucket
	 * 
	 * @return a stringified verison of the arithmetic median value.
	 */
	public String getAverageResult() {
		int num = 0;
		float sum = 0;
		for (Result result : results) {
			sum += makeFloat(result.get("resultat"));
			num += 1;
		}
		float avg = 100 * sum / num;
		float ret = ((float) Math.round(avg)) / 100f;
		return Float.toString(ret);
	}

	private float makeFloat(String s) {
		if (s.startsWith("<")) {
			return makeFloatInternal(s.substring(1).trim());
		} else if (s.startsWith(">")) {
			return makeFloatInternal(s.substring(1).trim());
		} else {
			return makeFloatInternal(s);
		}
	}

	private float makeFloatInternal(String s) {
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
			float ret = Integer.parseInt(einer) + fr;
			return ret;
		} else {
			return -1.0f;
		}
	}

	/**
	 * Ast whether a (stringified) value is pathologic for the Item in this
	 * bucket
	 * 
	 * @param value
	 *            the value to check
	 * @return true if the value is pathologic (with respect to the norm range
	 *         of the Item and the gender of the Patient)
	 */
	public boolean isPathologic(String value) {
		String chopped = value.trim();
		float val = 0f;
		if (chopped.startsWith("<")) {
			val = 0;
		} else if (chopped.startsWith(">")) {
			val = Integer.MAX_VALUE;
		} else {
			val = makeFloat(chopped);
		}
		int index = 0;
		if (pat.getGender() == Gender.FEMALE) {
			index = 2;
		}
		if ((val < refBounds[index]) || (val > refBounds[index + 1])) {
			return true;
		} else {
			return false;
		}
	}
}

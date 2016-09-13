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

import java.sql.ResultSet;

import ch.elexis.core.ui.util.Log;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.SimpleObject;
import ch.rgw.tools.StringTool;

public class Item extends SimpleObject implements Comparable<Item> {
	//final String ntyp = Integer.toString(LabItemTyp.NUMERIC.ordinal());
	static final int LOWER_BOUND_MALE = 0;
	static final int UPPER_BOUND_MALE = 1;
	static final int LOWER_BOUND_FEMALE = 2;
	static final int UPPER_BOUND_FEMALE = 3;
	
	Log log = Log.get(getClass().getName());
	
	private static final String[] fields = {
		"ID", "titel", "kuerzel", "Gruppe", "prio", "RefMann", "RefFrauOrTx", "Typ", "Einheit"
	};
	
	private float[] refBounds = new float[4];
	
	@Override
	public String[] getFields(){
		return fields;
	}
	
	public float[] getRefRange(){
		return refBounds;
	}
	
	public Item(ResultSet res){
		load(res);
		if(get("typ")==null){
			set("typ","NULL");
		}
		if (get("typ").equals("0")) {
			makeBounds(get("refMann"), LOWER_BOUND_MALE);
			makeBounds(get("refFrauOrTx"), LOWER_BOUND_FEMALE);
		}
	}
	
	public Item(String id){
		for (String field : fields) {
			props.put(field.toLowerCase(), "?");
		}
	}
	
	private void makeBounds(String val, int index){
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
	 * Ask whether a (stringified) value is pathologic for the Item in this bucket
	 * 
	 * @param value
	 *            the value to check
	 * @return true if the value is pathologic (with respect to the norm range of the Item and the
	 *         gender of the Patient)
	 */
	public boolean isPathologic(Patient pat, String value){
		if (value == null) {
			return false;
		}
		String chopped = value.trim();
		float val = 0f;
		if (chopped.startsWith("<")) {
			val = 0;
		} else if (chopped.startsWith(">")) {
			val = Integer.MAX_VALUE;
		} else {
			val = makeFloat(chopped);
		}
		int index = LOWER_BOUND_MALE;
		if (!pat.getGeschlecht().equals("m")) {
			index = LOWER_BOUND_FEMALE;
		}
		if ((val < refBounds[index]) || (val > refBounds[index + 1])) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(Item o){
		int gcomp = compare(o, "gruppe");
		return gcomp == 0 ? compare(o, "prio") : gcomp;
	}
	
	public boolean isEqual(Item o){
		String u = get("einheit");
		String ou = o.get("einheit");
		if (u == null && ou != null) {
			return false;
		}
		if (u != null && ou == null) {
			return false;
		}
		if (o == null || o.equals(ou)) {
			for (int i = 0; i < refBounds.length; i++) {
				if (refBounds[i] != o.refBounds[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
}

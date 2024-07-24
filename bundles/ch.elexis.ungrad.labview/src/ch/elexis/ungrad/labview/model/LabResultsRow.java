/*******************************************************************************
 * Copyright (c) 2016-2024 by G. Weirich
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

import java.util.SortedSet;
import java.util.TreeSet;

import ch.elexis.core.model.IPatient;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class LabResultsRow implements Comparable<LabResultsRow> {
	Item item;
	IPatient patient;
	SortedSet<Result> results;
	
	public LabResultsRow(Item item, IPatient pat){
		this.item = item;
		results = new TreeSet<>();
		this.patient = pat;
	}
	
	public void add(Result result){
		results.add(result);
	}
	
	public Result get(int index){
		if (results.size() > index) {
			return (Result) (results.toArray()[index]);
		} else {
			return null;
		}
	}
	
	public Result get(TimeTool date){
		for (Result res : results) {
			if (new TimeTool(res.get("datum")).isEqual(date)) {
				return res;
			}
		}
		return null;
	}
	
	public Result[] getResults(){
		return results.toArray(new Result[0]);
	}
	
	public Item getItem(){
		return item;
	}
	
	public IPatient getPatient(){
		return patient;
	}
	
	@Override
	public int compareTo(LabResultsRow r){
		return item.compareTo(r.item);
	}
	
	public boolean hasRelevantResults(){
		TimeTool now = new TimeTool();
		now.add(TimeTool.HOUR, -365 * 24);
		TimeTool cmp = new TimeTool();
		for (Result result : results) {
			if (cmp.set(result.get("datum"))) {
				if (cmp.isAfter(now)) {
					if (item.isPathologic(patient, result.get("resultat"))) {
						return true;
					}
				}
			}
			
		}
		return false;
	}
	
	public Result[] getBoundsBefore(TimeTool limit){
		String lim = limit.toString(TimeTool.DATE_GER);
		Result[] minmax = new Result[2];
		float min = Float.MAX_VALUE;
		float max = 0f;
		for (Result result : results) {
			TimeTool date = new TimeTool(result.get("datum"));
			String check = date.toString(TimeTool.DATE_GER);
			if (date.isBefore(limit)) {
				float cmp = Item.makeFloat(result.get("resultat"));
				if (cmp > max) {
					max = cmp;
					minmax[1] = result;
				}
				if (cmp < min) {
					min = cmp;
					minmax[0] = result;
				}
			}
		}
		return minmax;
	}
	
}

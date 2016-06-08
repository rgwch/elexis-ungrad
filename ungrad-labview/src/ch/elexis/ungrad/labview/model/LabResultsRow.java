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

import java.util.SortedSet;
import java.util.TreeSet;

import ch.elexis.data.Patient;

public class LabResultsRow implements Comparable{
	Item item;
	Patient patient;
	SortedSet<Result> results;

	public LabResultsRow(Item item, Patient pat){
		this.item=item;
		results=new TreeSet<Result>();
		this.patient=pat;
	}
	public void add(Result result){
		results.add(result);
	}
	public Result get(int index){
		if(results.size()>index){
			return (Result) (results.toArray()[index]);
		}else{
			return null;
		}
	}
	public Item getItem(){
		return item;
	}
	public Patient getPatient() {
		return patient;
	}
	@Override
	public int compareTo(Object o) {
		LabResultsRow r=(LabResultsRow)o;
		return item.compareTo(r.item);
	}

}

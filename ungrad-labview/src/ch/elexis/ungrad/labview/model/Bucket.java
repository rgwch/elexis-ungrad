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

import ch.elexis.ungrad.Util;

/**
 * A Bucket is a collection of all LabResults for a given LabItem and a given Patient. It can
 * decide, whether a result is pathologic, and it can perform some statistics.
 * 
 * @author gerry
 * 
 */
public class Bucket {
	
	Item item;
	List<Result> results = new ArrayList<Result>();
	
	Bucket(Item item){
		Util.require(item != null, "Bucket: item must not be null");
		this.item = item;
	}
	
	/**
	 * Add a Result to the bucket
	 * 
	 * @param result
	 *            The Result to add
	 */
	void addResult(Result result){
		results.add(result);
	}
	
	/**
	 * Get the number of Results in the Bucket.
	 * 
	 * @return a positive Integer
	 */
	public int getResultCount(){
		return results.size();
	}
	
	/**
	 * return the numerically lowest Result in the Bucket
	 * 
	 * @return a stringified version of the lowest Result
	 */
	public String getMinResult(){
		float cmp = Float.MAX_VALUE;
		for (Result result : results) {
			float val = Item.makeFloat(result.get("resultat"));
			if (val < cmp) {
				cmp = val;
			}
		}
		return cmp == Float.MAX_VALUE ? "" : Float.toString(cmp);
	}
	
	/**
	 * Get the numerically highest Result in the Bucket
	 * 
	 * @return a stringified versiuon of the highest Result.
	 */
	public String getMaxResult(){
		float cmp = 0;
		for (Result result : results) {
			float val = Item.makeFloat(result.get("resultat"));
			if (val > cmp) {
				cmp = val;
			}
		}
		return cmp == 0 ? "" : Float.toString(cmp);
	}
	
	/**
	 * Get the median value of all Results in the bucket
	 * 
	 * @return a stringified verison of the arithmetic median value.
	 */
	public String getAverageResult(){
		int num = 0;
		float sum = 0;
		for (Result result : results) {
			sum += Item.makeFloat(result.get("resultat"));
			num += 1;
		}
		float avg = 100 * sum / num;
		float ret = ((float) Math.round(avg)) / 100f;
		return num == 0 ? "" : Float.toString(ret);
	}
	
}

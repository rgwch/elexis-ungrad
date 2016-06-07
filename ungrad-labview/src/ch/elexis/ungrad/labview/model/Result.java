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

public class Result implements Comparable<Result>{

	
	String itemId, date,time,result,comment;
	
	public Result(String id, String dat,String zeit, String res, String com){
		itemId=id;
		date=dat;
		time=zeit;
		result=res;
		comment=com;
	}
	@Override
	public int compareTo(Result o) {
		if(date.equals(o.date)){
			if(time==null){
				return 1;
			}else if(o.time==null){
				return -1;
			}else{
				return time.compareTo(o.time);
			}
		}else{
			return date.compareTo(o.date);
		}
	}
	
	
}

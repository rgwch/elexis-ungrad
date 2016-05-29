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

package ch.rgw.elexis.docmgr_lucinda.model;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.rgw.tools.TimeTool;

/**
 * Candy class to avoid repetitive casts from Object to Map<String,Object>
 * 
 * @author gerry
 * 
 */
public class Document {
	private Map<String, Object> map;

	@SuppressWarnings("unchecked")
	public Document(Object src) {
		map = (Map<String, Object>) src;
	}

	public Document(){
		map=new HashMap<String,Object>();
	}
	
	public Map<String,Object> toMap(){
		return map;
	}
	
	public String get(String field) {
		Object o=map.get(field);
		if(o==null || !(o instanceof String)){
			return ""; //$NON-NLS-1$
		}else{
			return (String)o;
		}
	}

	public void put(String field, String value) {
		map.put(field, value);
	}

	public void put(String field, byte[] value){
		map.put(field, value);
	}
	
	public TimeTool getDate() {
		TimeTool tt = try_date(get("date")); //$NON-NLS-1$
		if (tt == null) {
			tt = try_date(get("parseDate")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_date(get("meta:creation-date")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_titledate(get("title")); //$NON-NLS-1$
		}
		return tt == null ? new TimeTool() : tt;
	}

	private TimeTool try_titledate(String d) {
		d = d.replaceAll("[_\\.]", "-"); //$NON-NLS-1$ //$NON-NLS-2$
		final Pattern p1 = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}"); //$NON-NLS-1$
		Matcher m1 = p1.matcher(d);
		if (m1.find()) {
			return new TimeTool(m1.group());
		}
		final Pattern p2 = Pattern.compile("\\d{1,2}-\\d{1,2}-\\d{2,4}"); //$NON-NLS-1$
		Matcher m2 = p2.matcher(d);
		if (m2.find()) {
			return new TimeTool(m2.group().replaceAll("-", ".")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	private TimeTool try_date(String d) {
		if (d != null) {
			try {
				return DateParser.parse(d);

			} catch (ParseException e) {
				// never mind
			}
		}
		return null;
	}

}

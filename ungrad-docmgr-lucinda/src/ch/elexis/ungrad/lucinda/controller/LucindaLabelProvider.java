/*******************************************************************************
 * Copyright (c) 2016-2020 by G. Weirich
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

package ch.elexis.ungrad.lucinda.controller;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.model.DateParser;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import java.util.Map;

public class LucindaLabelProvider extends TableLabelProvider {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		Map e = (Map) element;
		switch (columnIndex) {
		case 0:
			return (String)e.get(Preferences.FLD_LUCINDA_DOCTYPE);
		case 1:
			return (String)e.get("lastname"); //$NON-NLS-1$
		case 2:
			return getDate(e).toString(TimeTool.DATE_GER);
		case 3:
			String cat = (String)e.get("category");//$NON-NLS-1$
			if (StringTool.isNothing(cat)) {
				cat = (String)e.get("Kategorie");//$NON-NLS-1$
			}
			if (StringTool.isNothing(cat)) {
				return (String)e.get("title"); //$NON-NLS-1$
			} else {
				return cat + ": " + e.get("title"); //$NON-NLS-1$
			}
		default:
			return "?"; //$NON-NLS-1$
		}
	}

	/**
	 * Try to read the document date from document's metadata. The Method tries several
	 * fields
	 * @param jo the Document metadata
	 * @return
	 */
	public TimeTool getDate(Map jo) {
		TimeTool tt = try_date(jo.get("last-modified")); //$NON-NLS-1$
		if (tt == null) {
			tt = try_date(jo.get("meta:creation-date")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_titledate(jo.get("title")); //$NON-NLS-1$
		}
		if(tt==null){
			tt=try_date(jo.get("date"));
		}
		if (tt == null) {
			tt = try_date(jo.get("meta:save-date")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_date(jo.get("last-save-date")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_date(jo.get("creation-date")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_date(jo.get("parseDate")); //$NON-NLS-1$
		}
		
		return tt == null ? new TimeTool() : tt;
	}

	private TimeTool try_titledate(Object ddo) {
		String d = ((String)ddo).replaceAll("[_\\.]", "-"); //$NON-NLS-1$ //$NON-NLS-2$
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

	private TimeTool try_date(Object d) {
		if (d != null) {
			try {
				return DateParser.parse((String)d);

			} catch (ParseException e) {
				// never mind
			}
		}
		return null;
	}

}

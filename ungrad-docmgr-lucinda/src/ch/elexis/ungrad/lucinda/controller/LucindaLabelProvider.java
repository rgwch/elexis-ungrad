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

package ch.elexis.ungrad.lucinda.controller;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.model.DateParser;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import io.vertx.core.json.JsonObject;

public class LucindaLabelProvider extends TableLabelProvider {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		JsonObject e = (JsonObject) element;
		switch (columnIndex) {
		case 0:
			return e.getString(Preferences.FLD_LUCINDA_DOCTYPE);
		case 1:
			return e.getString("lastname"); //$NON-NLS-1$
		case 2:
			return getDate(e).toString(TimeTool.DATE_GER);
		case 3:
			String cat = e.getString("category");//$NON-NLS-1$
			if (StringTool.isNothing(cat)) {
				cat = e.getString("Kategorie");//$NON-NLS-1$
			}
			if (StringTool.isNothing(cat)) {
				return e.getString("title"); //$NON-NLS-1$
			} else {
				return cat + ": " + e.getString("title"); //$NON-NLS-1$
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
	public TimeTool getDate(JsonObject jo) {
		TimeTool tt = try_date(jo.getString("last-modified")); //$NON-NLS-1$
		if (tt == null) {
			tt = try_date(jo.getString("meta:creation-date")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_titledate(jo.getString("title")); //$NON-NLS-1$
		}
		if(tt==null){
			tt=try_date(jo.getString("date"));
		}
		if (tt == null) {
			tt = try_date(jo.getString("meta:save-date")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_date(jo.getString("last-save-date")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_date(jo.getString("creation-date")); //$NON-NLS-1$
		}
		if (tt == null) {
			tt = try_date(jo.getString("parseDate")); //$NON-NLS-1$
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

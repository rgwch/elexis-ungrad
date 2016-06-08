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
import java.util.HashMap;
import java.util.Map;

import ch.elexis.core.ui.util.Log;
import ch.elexis.ungrad.SimpleObject;

public class Item extends SimpleObject implements Comparable<Item> {
	Log log = Log.get(getClass().getName());

	private static final String[] fields = { "ID", "titel", "kuerzel", "Gruppe", "prio", "RefMann", "RefFrauOrTx",
			"Typ", "Einheit" };

	@Override
	public String[] getFields() {
		return fields;
	}

	public Item(ResultSet res) {
		load(res);
	}

	public Item(String id) {
		for (String field : fields) {
			props.put(field.toLowerCase(), "?");
		}
	}

	@Override
	public int compareTo(Item o) {
		int gcomp = compare(o, "gruppe");
		return gcomp == 0 ? compare(o, "prio") : gcomp;
	}

}

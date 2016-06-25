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

import ch.elexis.ungrad.SimpleObject;

public class Result extends SimpleObject implements Comparable<Result> {
	public static final String[] fields = { "ID","ItemID", "Datum", "Zeit", "Resultat", "Kommentar" };

	public Result(float result){
		set("Resultat",Float.toString(result));
	}
	public Result(ResultSet res) {
		load(res);
	}

	@Override
	public int compareTo(Result o) {
		int cdat = compare(o, "datum");
		return cdat == 0 ? compare(o, "zeit") : cdat;

	}

	@Override
	public String[] getFields() {
		return fields;
	}

}

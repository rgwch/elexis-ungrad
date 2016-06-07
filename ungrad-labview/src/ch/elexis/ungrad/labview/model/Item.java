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

public class Item implements Comparable<Item> {
	public Item(String ID, String titel, String kuerzel, String group, String prio, String RefMann, String RefFrauOrTx, String typ) {
		this.titel = titel;
		this.kuerzel = kuerzel;
		this.refMann = RefMann;
		this.refFrauOrTx = RefFrauOrTx;
		this.gruppe = group==null ? " ":group;
		this.prio = prio;
		this.id=ID;
		this.type=typ;
		psid=titel+kuerzel+refMann+refFrauOrTx;
	}

	public String id;
	public String psid;
	public String titel;
	public String kuerzel;
	public String refMann;
	public String refFrauOrTx;
	public String gruppe;
	public String prio;
	public String type;
	

	@Override
	public int compareTo(Item o) {
		if (o == null) {
			return 1;
		}
		if (gruppe.equals(o.gruppe)) {
			return prio.compareTo(o.prio);
		} else {
			return gruppe.compareTo(o.gruppe);
		}
	}

}

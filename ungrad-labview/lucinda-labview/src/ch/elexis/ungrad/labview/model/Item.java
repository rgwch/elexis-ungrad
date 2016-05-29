package ch.elexis.ungrad.labview.model;

public class Item implements Comparable<Item> {
	public Item(String titel, String kuerzel, String Gruppe, String Prio, String RefMann, String RefFrauOrTx, String ID) {
		this.titel = titel;
		this.kuerzel = kuerzel;
		this.refMann = RefMann;
		this.refFrauOrTx = RefFrauOrTx;
		this.gruppe = gruppe;
		this.prio = prio;
		this.id=ID;
		psid=titel+kuerzel+refMann+refFrauOrTx;
	}

	String id;
	String psid;
	String titel;
	String kuerzel;
	String refMann;
	String refFrauOrTx;
	String gruppe;
	String prio;

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

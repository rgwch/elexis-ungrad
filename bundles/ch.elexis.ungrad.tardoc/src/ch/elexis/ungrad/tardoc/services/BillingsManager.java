package ch.elexis.ungrad.tardoc.services;

import java.time.LocalDate;

import ch.elexis.core.model.IEncounter;
import ch.elexis.data.Konsultation;
import ch.elexis.ungrad.tardoc.views.TardocKonsView;

public class BillingsManager {
	private IEncounter kons;
	private Konsultation b;
	private TardocKonsView tkv;

	public BillingsManager(TardocKonsView view) {
		this.tkv=view;
	}

	public void setEncounter(IEncounter kons) {
		this.kons = kons;
		this.b = Konsultation.load(kons.getId());

	}
	
	public boolean isToday() {
		LocalDate konsDate = kons.getDate();
		return konsDate.isEqual(LocalDate.now());
	}
}

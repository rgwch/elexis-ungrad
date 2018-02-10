package ch.elexis.ungrad.qrbills;

import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.rgw.crypt.BadParameterException;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class BillDetails {
	TarmedACL ta = TarmedACL.getInstance();

	Rechnung rn;
	Fall fall;
	Patient patient;
	Money amount;
	Kontakt biller;
	Kontakt adressat;
	String IBAN = "FAKE";
	String currency;
	Kontakt bank;
	String qrIBAN;
	String biller_address;
	String addressee;
	String dateDue;
	String firstDate;
	String lastDate;
	int numCons;;

	public BillDetails(Rechnung rn) throws BadParameterException {
		checkNull(rn, "Rechnung");
		this.rn = rn;
		fall = rn.getFall();
		checkNull(fall, "Fall");
		patient = fall.getPatient();
		checkNull(patient, "Patient");
		amount = rn.getBetrag();
		checkNull(amount, "Amount");
		checkNull(rn.getMandant(), "Mandant");
		biller = rn.getMandant().getRechnungssteller();
		checkNull(biller, "Biller");
		adressat = patient;
		IBAN = (String) biller.getExtInfoStoredObjectByKey("IBAN");
		checkNull(IBAN, "IBAN");
		if (IBAN.length() != 21) {
			throw new BadParameterException("IBAN is not 21 Chars", 3);
		}
		if (!(IBAN.toLowerCase().startsWith("ch") || IBAN.toLowerCase().startsWith("li"))) {
			throw new BadParameterException("Only CH and LI IBANs allowed", 4);
		}
		currency = (String) biller.getExtInfoStoredObjectByKey(Messages.XMLExporter_Currency);
		if (StringTool.isNothing(currency)) {
			currency = "CHF";
		}
		bank = Kontakt.load(biller.getInfoString(ta.RNBANK));
		if (!bank.isValid()) {
			throw new BadParameterException("Bank was not valid", 2);
		}
		checkNull(rn.getNr(), "Bill Number");
		qrIBAN = StringTool.pad(StringTool.LEFT, '0', StringTool.addModulo10(rn.getNr()), 27);
		checkNull(biller.getPostAnschrift(), "Postanschrift");
		biller_address = biller.getPostAnschrift(true).trim().replaceAll("\\r", "").replaceAll("\\n+", "<br />");
		checkNull(adressat.getPostAnschrift(), "Postanschrift");
		addressee = adressat.getPostAnschrift(true).trim().replaceAll("\\r", "").replaceAll("\\n+", "<br />");
		TimeTool now = new TimeTool();
		now.addDays(30);
		dateDue = now.toString(TimeTool.DATE_GER);
		checkNull(rn.getDatumVon(), "From date");
		checkNull(rn.getDatumBis(), "Until date");
		checkNull(rn.getKonsultationen(), "Consultations list");
		firstDate = new TimeTool(rn.getDatumVon()).toString(TimeTool.DATE_GER);
		numCons = rn.getKonsultationen().size();
		lastDate = new TimeTool(rn.getDatumBis()).toString(TimeTool.DATE_GER);
	}

	void checkNull(Object o, String msg) throws BadParameterException {
		if (o == null) {
			throw new BadParameterException(msg + " was null", 1);
		}
	}
}

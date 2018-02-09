package ch.elexis.ungrad.qrbills;

import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
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
	
	public BillDetails(Rechnung rn){
		this.rn = rn;
		fall = rn.getFall();
		patient = fall.getPatient();
		amount = rn.getBetrag();
		biller = rn.getMandant().getRechnungssteller();
		adressat = patient;
		currency = (String) biller.getExtInfoStoredObjectByKey(Messages.XMLExporter_Currency);
		if (StringTool.isNothing(currency)) {
			currency = "CHF";
		}
		bank = Kontakt.load(biller.getInfoString(ta.RNBANK));
		qrIBAN = StringTool.pad(StringTool.LEFT, '0', StringTool.addModulo10(rn.getNr()), 27);
		biller_address=biller.getPostAnschrift(true).trim().replaceAll("\\r","").replaceAll("\\n+", "<br />");
		addressee=adressat.getPostAnschrift(true).trim().replaceAll("\\r","").replaceAll("\\n+", "<br />");
		TimeTool now=new TimeTool();
		now.addDays(30);
		dateDue=now.toString(TimeTool.DATE_GER);
		
	}
}

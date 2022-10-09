package ch.elexis.ungrad.qrbills;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnungssteller;
import ch.rgw.tools.Money;

public class QRBillDetails {
	String qrIBAN;
	String qrReference;
	Money amountTotalWithCharges,
	amountDue, //original amount before charges and payments 
	amountPaid,
	amountCharges;
	Rechnungssteller biller;
	Kontakt adressat;
}

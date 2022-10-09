package ch.elexis.ungrad.qrbills;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.*;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.rgw.crypt.BadParameterException;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class QRBillDetails {
	public Rechnung rn;
	public Fall fall;
	public Patient patient;
	public String qrIBAN = "CH000NUR00ZUR00DEMO00";
	public String qrReference;
	public Money amountTotalWithCharges, amountDue, //original amount before charges and payments 
			amountPaid, amountCharges;
	public Mandant mandator;
	public Rechnungssteller biller;
	public String biller_address;
	public Kontakt adressat;
	public String addressee;
	public List<Zahlung> charges = new ArrayList<Zahlung>(); // all charges
	public String currency="CHF";
	public String dateDue;
	
	TarmedACL ta = TarmedACL.getInstance();
	
	public QRBillDetails(Rechnung rn) throws BadParameterException{
		this.rn = (Rechnung) checkNull(rn, "Rechnung");
		fall = (Fall) checkNull(rn.getFall(), "Fall");
		patient = (Patient) checkNull(fall.getPatient(), "Patient");
		mandator = (Mandant) checkNull(rn.getMandant(), "Mandant");
		biller = (Rechnungssteller) checkNull(mandator.getRechnungssteller(), "Rechnungssteller");
		adressat = (Kontakt) checkNull(fall.getInvoiceRecipient(), "Adressat");
		dateDue = rn.getRnDatumFrist();
		amountPaid=new Money();
	}
	
	public String getFormatted(String orig){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 20; i += 4) {
			sb.append(orig.substring(i, i + 4)).append(" ");
		}
		return sb.append(orig.substring(20)).toString();
	}
	
	public String createQRReference(String id) throws BadParameterException{
		String usr = rn.getRnId();
		int space = 26 - usr.length() - id.length();
		if (space < 0) {
			throw new BadParameterException("id for QR reference too long", 3);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(id).append(StringTool.filler("0", space)).append(usr);
		qrReference = StringTool.addModulo10(sb.toString());
		return qrReference;
	}
	
	public String combinedAddress(Kontakt k){
		StringBuilder sb = new StringBuilder();
		sb.append(k.get(Kontakt.FLD_NAME1) + " " + k.get(Kontakt.FLD_NAME2)).append("<br />")
			.append(k.get(Kontakt.FLD_STREET)).append("<br />")
			.append(k.get(Kontakt.FLD_ZIP) + " " + k.get(Kontakt.FLD_PLACE));
		return sb.toString();
	}
	
	public void addCharges(){
		amountTotalWithCharges = new Money(amountDue);
		amountCharges = new Money();
		for (Zahlung z : rn.getZahlungen()) {
			Money betrag = z.getBetrag();
			if (betrag.isNegative()) {
				charges.add(z);
				amountCharges.subtractMoney(betrag);
			} else {
				amountPaid.addMoney(betrag);
			}
			amountTotalWithCharges.subtractMoney(betrag);
		}
		amountTotalWithCharges.roundTo5();
	}
	
	public Object checkNull(Object o, String msg) throws BadParameterException{
		if (o == null) {
			throw new BadParameterException(msg + " was null", 1);
		}
		return o;
	}
	
	public void writePDF(File inputHTML, File outputPDF)
		throws FileNotFoundException, IOException, PrinterException{
		FileOutputStream fout = new FileOutputStream(outputPDF);
		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.useFastMode();
		builder.withFile(inputHTML);
		builder.toStream(fout);
		builder.run();
	}
	
}

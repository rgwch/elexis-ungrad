/*******************************************************************************
 * Copyright (c) 2022-2024, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.ungrad.qrbills;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.data.*;
import ch.elexis.ungrad.pdf.Manager;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;

/**
 * Collect some data from different sources to create a valid QR Bill
 * 
 * @author gerry
 *
 */
public class QRBillDetails {
	public Rechnung rn;
	public Fall fall;
	public Patient patient;
	public String qrIBAN = "CH000NUR00ZUR00DEMO00";
	public String qrReference;
	public Money amountTotalWithCharges, amountDue, // original amount before charges and payments
			amountPaid, amountCharges;
	public Mandant mandator;
	public Rechnungssteller biller;
	public String biller_address;
	public Kontakt adressat;
	public String addressee;
	public List<Zahlung> charges = new ArrayList<Zahlung>(); // all charges
	public String currency = "CHF";
	public String dateDue;

	TarmedACL ta = TarmedACL.getInstance();

	/**
	 * Analyze a "Rechnung" object
	 * 
	 * @param rn
	 * @throws BadParameterException
	 */
	public QRBillDetails(Rechnung rn) throws Exception {
		this.rn = (Rechnung) checkNull(rn, "Rechnung");
		fall = (Fall) checkNull(rn.getFall(), "Fall");
		patient = (Patient) checkNull(fall.getPatient(), "Patient");
		mandator = (Mandant) checkNull(rn.getMandant(), "Mandant");
		biller = (Rechnungssteller) checkNull(mandator.getRechnungssteller(), "Rechnungssteller");
		adressat = (Kontakt) checkNull(fall.getInvoiceRecipient(), "Adressat");
		dateDue = rn.getRnDatumFrist();
		amountPaid = new Money();
	}

	public String getFormatted(String orig) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 20; i += 4) {
			sb.append(orig.substring(i, i + 4)).append(" ");
		}
		return sb.append(orig.substring(20)).toString();
	}

	public String createQRReference(String id) throws Exception {
		String usr = rn.getRnId();
		int space = 26 - usr.length() - id.length();
		if (space < 0) {
			throw new Exception("id for QR reference too long");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(id).append(StringTool.filler("0", space)).append(usr);
		qrReference = StringTool.addModulo10(sb.toString());
		return qrReference;
	}

	public String combinedAddress(Kontakt k) {
		StringBuilder sb = new StringBuilder();
		sb.append(k.get(Kontakt.FLD_NAME2) + " " + k.get(Kontakt.FLD_NAME1)).append("<br />")
				.append(k.get(Kontakt.FLD_STREET)).append("<br />")
				.append(k.get(Kontakt.FLD_ZIP) + " " + k.get(Kontakt.FLD_PLACE));
		return sb.toString();
	}

	public void addCharges() {
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

	public Object checkNull(Object o, String msg) throws Exception {
		if (o == null) {
			throw new Exception(msg + " was null");
		}
		return o;
	}

	public void writePDF(File inputHTML, File outputPDF) throws FileNotFoundException, IOException, PrinterException {
		Manager pdfManager = new Manager();
		pdfManager.createPDF(inputHTML, outputPDF);
	}

}

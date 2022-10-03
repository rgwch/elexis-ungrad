/*******************************************************************************
 * Copyright (c) 2018-2022 by G. Weirich
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
package ch.elexis.ungrad.qrbills;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.medevit.elexis.tarmed.model.TarmedJaxbUtil;
import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.TarmedRechnung.XMLExporterTiers;
import ch.elexis.base.ch.ebanking.esr.ESR;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IRnOutputter.TYPE;
import ch.elexis.data.*;
import ch.elexis.tarmed.printer.XML44Services;
import ch.elexis.tarmed.printer.XMLPrinterUtil;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.fd.invoice440.request.*;
import ch.rgw.crypt.BadParameterException;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class BillDetails {
	public static int FALL_UVG = 1;
	public static int FALL_IVG = 2;
	public static int FALL_KVG = 3;
	public static int FALL_MVG = 4;
	TarmedACL ta = TarmedACL.getInstance();
	String outputDirPDF, outputDirXML;
	RequestType request;
	String paymentMode = XMLExporter.TIERS_PAYANT;
	String documentId;
	Rechnung rn;
	Fall fall;
	Mandant mandator;
	Rechnungssteller biller;
	String biller_address;
	Kontakt adressat;
	String addressee;
	Kontakt guarantor;
	Kontakt zuweiser;
	TYPE type;

	int fallType = FALL_KVG;
	Patient patient;
	Money amountTarmed, amountDrug, amountLab, amountMigel, amountPhysio, amountUnclassified, 
	amountDue, //original amount before charges and payments 
	amountPaid,
	amountCharges,
	amountTotalWithCharges;  // end amount with charges and minus payments
	List<Zahlung> charges = new ArrayList<Zahlung>(); // all charges
	TreatmentType treatments;
	ReminderType reminders;
	String qrIBAN = "CH000NUR00ZUR00DEMO00";
	String formattedIban;
	String currency;
	Kontakt bank;
	String qrReference;
	String formattedReference;
	String dateDue;
	String firstDate;
	String lastDate;
	String caseDate;
	String caseNumber;
	String tcCode;
	String remarks;
	int numCons;
	ServicesType services;
	BalanceType balance;

	private static Logger logger = LoggerFactory.getLogger(BillDetails.class);

	public String combinedAddress(Kontakt k) {
		StringBuilder sb = new StringBuilder();
		sb.append(k.get(Kontakt.FLD_NAME1) + " " + k.get(Kontakt.FLD_NAME2)).append("<br />")
				.append(k.get(Kontakt.FLD_STREET)).append("<br />")
				.append(k.get(Kontakt.FLD_ZIP) + " " + k.get(Kontakt.FLD_PLACE));
		return sb.toString();
	}

	public BillDetails(Rechnung rn, TYPE type, boolean bStrict) throws Exception {
		this.rn = (Rechnung) checkNull(rn, "Rechnung");
		fall = (Fall) checkNull(rn.getFall(), "Fall");
		this.type = type;
		patient = (Patient) checkNull(fall.getPatient(), "Patient");
		mandator = (Mandant) checkNull(rn.getMandant(), "Mandant");
		biller = (Rechnungssteller) checkNull(mandator.getRechnungssteller(), "Rechnungssteller");
		outputDirPDF = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR_PDF, CoreHub.getTempDir().getAbsolutePath());
		outputDirXML = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR_XML, CoreHub.getTempDir().getAbsolutePath());
		File xmlfile = new File(outputDirXML, rn.getNr() + ".xml");
		XMLExporter xmlex = new XMLExporter();

		Document xmlRn = xmlex.doExport(rn, xmlfile.getAbsolutePath(), type, !bStrict);
		if(rn.getStatus() == RnStatus.FEHLERHAFT) {
			throw new Exception("Fehler in Rechnung "+rn.getNr());
		}

		request = TarmedJaxbUtil.unmarshalInvoiceRequest440(xmlRn);
		if (request == null) {
			logger.error("Could not unmarshall xml document for invoice");
			throw new Exception("Bad xml structure in " + rn.getNr());
		}
		BodyType body = request.getPayload().getBody();
		balance = body.getBalance();
		services = body.getServices();
		InvoiceType invoice = request.getPayload().getInvoice();
		TimeTool date = new TimeTool(invoice.getRequestDate().toString());
		documentId = invoice.getRequestId() + " - " + date.toString(TimeTool.DATE_GER) + " "
				+ date.toString(TimeTool.TIME_FULL);
		remarks = body.getRemark();

		paymentMode = XMLExporter.TIERS_PAYANT;
		if (body.getTiersGarant() != null) {
			paymentMode = XMLExporter.TIERS_GARANT;
		}
		XML44Services xmlservices = new XML44Services(services);
		amountDue=new Money();
		amountTarmed = xmlservices.getTarmedMoney();
		amountDue.addMoney(amountTarmed);
		amountDrug = xmlservices.getDrugMoney();
		amountDue.addMoney(amountDrug);
		amountLab = xmlservices.getLabMoney();
		amountDue.addMoney(amountLab);
		amountMigel = xmlservices.getMigelMoney();
		amountDue.addMoney(amountMigel);
		amountPhysio = xmlservices.getParamedMoney();
		amountDue.addMoney(amountPhysio);
		amountUnclassified = xmlservices.getOtherMoney();
		amountDue.addMoney(amountUnclassified);
		// amountDue = new Money(balance.getAmountDue());
		amountTotalWithCharges = new Money(amountDue);

		amountPaid=new Money();
		amountCharges=new Money();
		for (Zahlung z : rn.getZahlungen()) {
			Money betrag=z.getBetrag();
			if (betrag.isNegative()) {
				charges.add(z);
				amountCharges.subtractMoney(betrag);
			}else {
				amountPaid.addMoney(betrag);
			}
			amountTotalWithCharges.subtractMoney(betrag);
		}
		amountTotalWithCharges.roundTo5();
		// amountPaid = new Money(balance.getAmountPrepaid());
		GarantType eTiers = body.getTiersGarant();
		adressat=fall.getInvoiceRecipient();
		checkNull(adressat,"Adressat");
		if (eTiers == null) {
			paymentMode = XMLExporter.TIERS_PAYANT;
			guarantor = fall.getCostBearer();
			if (guarantor == null) {
				guarantor = patient;
			}
		} else {
			paymentMode = XMLExporter.TIERS_GARANT;
			guarantor = XMLExporterTiers.getGuarantor(XMLExporter.TIERS_GARANT, patient, fall);
		}
		checkNull(guarantor, "Garant");
		if (body.getUvg() != null) {
			fallType = FALL_UVG;
		} else if (body.getIvg() != null) {
			fallType = FALL_IVG;
		} else if (body.getMvg() != null) {
			fallType = FALL_MVG;
		} else {
			fallType = FALL_KVG;
		}
		if (TarmedRequirements.hasTCContract(biller) && paymentMode.equals(XMLExporter.TIERS_GARANT)) {
			tcCode = TarmedRequirements.getTCCode(biller);
		} else if (paymentMode.equals(XMLExporter.TIERS_PAYANT)) {
			tcCode = "01";
		}

		XMLPrinterUtil.updateContext(rn, fall, patient, mandator, biller, paymentMode);

		caseDate = getFDatum();
		caseNumber = getFNummer();
		zuweiser = fall.getRequiredContact("Zuweiser");
		treatments = body.getTreatment();
		reminders = request.getPayload().getReminder();
		String patnr = (String) checkNull(patient.getPatCode(), "PatientNr.");
		qrIBAN = (String) biller.getExtInfoStoredObjectByKey(PreferenceConstants.QRIBAN);
		checkNull(qrIBAN, "IBAN");
		if (qrIBAN.length() != 21) {
			throw new BadParameterException("IBAN is not 21 Chars", 3);
		}
		if (!(qrIBAN.toLowerCase().startsWith("ch") || qrIBAN.toLowerCase().startsWith("li"))) {
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
		checkNull(biller.getPostAnschrift(), "Postanschrift");
		biller_address = biller.getPostAnschrift(true).trim().replaceAll("\\r", "").replaceAll("\\n+", "<br />");
		checkNull(biller_address, "Absender");
		checkNull(adressat.getPostAnschrift(), "Postanschrift");
		addressee = adressat.getPostAnschrift(true).trim().replaceAll("\\r", "").replaceAll("\\n+", "<br />");
		checkNull(addressee, "Anschrift");
		TimeTool now = new TimeTool();
		now.addDays(30);
		dateDue = now.toString(TimeTool.DATE_GER);
		checkNull(rn.getDatumVon(), "From date");
		checkNull(rn.getDatumBis(), "Until date");
		checkNull(rn.getKonsultationen(), "Consultations list");
		firstDate = new TimeTool(rn.getDatumVon()).toString(TimeTool.DATE_GER);
		numCons = rn.getKonsultationen().size();
		lastDate = new TimeTool(rn.getDatumBis()).toString(TimeTool.DATE_GER);
		ESR esr = new ESR((String) biller.getExtInfoStoredObjectByKey(ta.ESRNUMBER),
				(String) biller.getExtInfoStoredObjectByKey(ta.ESRSUB), rn.getRnId(), 27);
		formattedReference = esr.makeRefNr(true);
		qrReference = esr.makeRefNr(false);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 20; i += 4) {
			sb.append(qrIBAN.substring(i, i + 4)).append(" ");
		}
		formattedIban = sb.append(qrIBAN.substring(20)).toString();
		
	}

	private String getFDatum() {
		if (fallType == FALL_UVG) {
			String ret = fall.getInfoString("Unfalldatum");
			if (ret != null && !ret.isEmpty()) {
				return ret;
			}
		}
		if (fallType == FALL_IVG) {
			String ret = fall.getInfoString("Verfügungsdatum");
			if (ret != null && !ret.isEmpty()) {
				return ret;
			}
		}
		return fall.getBeginnDatum();
	}

	private String getFNummer() {
		if (fallType == FALL_UVG) {
			String ret = fall.getInfoString("Unfall-Nr.");
			if (ret != null && !ret.isEmpty()) {
				return ret;
			}
			ret = fall.getInfoString("Unfallnummer");
			if (ret != null && !ret.isEmpty()) {
				return ret;
			}
		}
		if (fallType == FALL_IVG) {
			String ret = fall.getInfoString("Verfügungs-Nr.");
			if (ret != null && !ret.isEmpty()) {
				return ret;
			}
			ret = fall.getInfoString("Verfügungsnummer");
			if (ret != null && !ret.isEmpty()) {
				return ret;
			}
		}
		return fall.getFallNummer();
	}

	Object checkNull(Object o, String msg) throws BadParameterException {
		if (o == null) {
			throw new BadParameterException(msg + " was null", 1);
		}
		return o;
	}

}

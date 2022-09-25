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
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
import ch.elexis.data.Fall.Tiers;
import ch.elexis.tarmed.printer.XML44Services;
import ch.elexis.tarmed.printer.XMLPrinterUtil;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.fd.invoice440.request.BalanceType;
import ch.fd.invoice440.request.BodyType;
import ch.fd.invoice440.request.GarantType;
import ch.fd.invoice440.request.InvoiceType;
import ch.fd.invoice440.request.ReminderType;
import ch.fd.invoice440.request.RequestType;
import ch.fd.invoice440.request.ServicesType;
import ch.fd.invoice440.request.TreatmentType;
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
	Kontakt adressat;
	Kontakt guarantor;
	Kontakt zuweiser;
	TYPE type;

	int fallType = FALL_KVG;
	Patient patient;
	Money amountTarmed, amountDrug, amountLab, amountMigel, amountPhysio, amountUnclassified, amountDue, amountPaid,
			amountTotal;
	TreatmentType treatments;
	ReminderType reminders;
	String qrIBAN = "CH000NUR00ZUR00DEMO00";
	String formattedIban;
	String currency;
	Kontakt bank;
	String qrReference;
	String formattedReference;
	String biller_address;
	String addressee;
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

	public BillDetails(Rechnung rn, TYPE type) throws Exception {
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

		Document xmlRn = xmlex.doExport(rn, xmlfile.getAbsolutePath(), type, true);

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
		amountTarmed = xmlservices.getTarmedMoney();
		amountDrug = xmlservices.getDrugMoney();
		amountLab = xmlservices.getLabMoney();
		amountMigel = xmlservices.getMigelMoney();
		amountPhysio = xmlservices.getParamedMoney();
		amountUnclassified = xmlservices.getOtherMoney();
		amountDue = new Money(balance.getAmountDue());

		double dReminder = balance.getAmountReminder();
		if (dReminder > 0) {
			amountDue.subtractMoney(new Money(dReminder));
		}
		amountPaid = new Money(balance.getAmountPrepaid());
		GarantType eTiers = body.getTiersGarant();
		if (eTiers == null) {
			paymentMode = XMLExporter.TIERS_PAYANT;
			guarantor = fall.getCostBearer();
			if (guarantor == null) {
				guarantor = patient;
			}
			adressat = guarantor;
		} else {
			paymentMode = XMLExporter.TIERS_GARANT;
			guarantor = XMLExporterTiers.getGuarantor(XMLExporter.TIERS_GARANT, patient, fall);
			adressat = patient;
		}
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
		fillDetails();
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

	private void fillDetails() throws BadParameterException {
		checkNull(rn.getMandant(), "Mandant");
		biller = rn.getMandant().getRechnungssteller();
		checkNull(biller, "Biller");
		adressat = patient;
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

	Object checkNull(Object o, String msg) throws BadParameterException {
		if (o == null) {
			throw new BadParameterException(msg + " was null", 1);
		}
		return o;
	}

}

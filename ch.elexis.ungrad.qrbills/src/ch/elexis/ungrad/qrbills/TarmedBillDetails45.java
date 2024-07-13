/*******************************************************************************
 * Copyright (c) 2024 by G. Weirich
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
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import at.medevit.elexis.tarmed.model.TarmedJaxbUtil;
import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.base.ch.ebanking.esr.ESR;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IRnOutputter.TYPE;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.fd.invoice450.request.ReminderType;
import ch.fd.invoice450.request.RequestType;
import ch.fd.invoice450.request.TreatmentType;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class TarmedBillDetails45 extends QRBillDetails {
	public static int FALL_UVG = 1;
	public static int FALL_IVG = 2;
	public static int FALL_KVG = 3;
	public static int FALL_MVG = 4;
	IConfigService cfg;
	String outputDirPDF, outputDirXML;
	RequestType request;
	String paymentMode = XMLExporter.TIERS_PAYANT;
	String documentId;

	IContact guarantor;
	IContact zuweiser;
	Kontakt bank;
	TYPE type;
	int fallType = FALL_KVG;
	Money amountTarmed, amountDrug, amountLab, amountMigel, amountPhysio, amountUnclassified;

	TreatmentType treatments;
	ReminderType reminders;

	String firstDate;
	String lastDate;
	String caseDate;
	String caseNumber;
	String tcCode;
	String remarks;
	int numCons;
	ch.fd.invoice450.request.ServicesType services;
	Document xmldom;
	private static Logger logger = LoggerFactory.getLogger(TarmedBillDetails45.class);

	@SuppressWarnings("deprecation")
	public TarmedBillDetails45(Rechnung rn, TYPE type, boolean bStrict) throws Exception {
		super(rn);
		this.type = type;
		cfg = ConfigServiceHolder.get();
		outputDirPDF = cfg.getLocal(PreferenceConstants.RNN_DIR_PDF, CoreHub.getTempDir().getAbsolutePath());
		outputDirXML = cfg.getLocal(PreferenceConstants.RNN_DIR_XML, CoreHub.getTempDir().getAbsolutePath());
		if (rn.getInvoiceState() == InvoiceState.DEFECTIVE) {
			throw new Exception("Fehler in Rechnung " + rn.getNr());
		}
		File xmlfile = new File(outputDirXML, rn.getNr() + ".xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		xmldom = dbf.newDocumentBuilder().parse(new FileInputStream(xmlfile));
		ch.fd.invoice450.request.RequestType request = TarmedJaxbUtil
				.unmarshalInvoiceRequest450(new FileInputStream(xmlfile));
		if (request == null) {
			logger.error("Could not unmarshall xml document for invoice");
			throw new Exception("Bad xml structure in " + rn.getNr());
		}
		ch.fd.invoice450.request.BodyType body = request.getPayload().getBody();
		services = body.getServices();
		XML45Services xmlservices = new XML45Services(services);
		amountDue = new Money();
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
		amountPaid = new Money();
		addCharges();
		ch.fd.invoice450.request.InvoiceType invoice = request.getPayload().getInvoice();
		TimeTool date = new TimeTool(invoice.getRequestDate().toString());
		documentId = invoice.getRequestId() + " - " + date.toString(TimeTool.DATE_GER) + " "
				+ date.toString(TimeTool.TIME_FULL);
		remarks = body.getRemark();

		paymentMode = XMLExporter.TIERS_PAYANT;
		if (body.getTiersGarant() != null) {
			paymentMode = XMLExporter.TIERS_GARANT;
		}
		caseDate = getFDatum();
		caseNumber = getFNummer();
		zuweiser = (IContact) fall.getRequiredContact("Zuweiser");
		treatments = body.getTreatment();
		reminders = request.getPayload().getReminder();
		// String patnr = (String) checkNull(patient.getPatCode(), "PatientNr.");
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
		bank = Kontakt.load(biller.getInfoString(ta.RNBANK));
	
		createReferences();
	}

	private void createReferences() throws Exception {
		qrIBAN = (String) biller.getExtInfoStoredObjectByKey(PreferenceConstants.QRIBAN);
		checkNull(qrIBAN, "IBAN");
		if (qrIBAN.length() != 21) {
			throw new Exception("IBAN is not 21 Chars");
		}
		if (!(qrIBAN.toLowerCase().startsWith("ch") || qrIBAN.toLowerCase().startsWith("li"))) {
			throw new Exception("Only CH and LI IBANs allowed");
		}
		currency = (String) biller.getExtInfoStoredObjectByKey(Messages.XMLExporter_Currency);
		if (StringTool.isNothing(currency)) {
			currency = "CHF";
		}

		ESR esr = new ESR((String) biller.getExtInfoStoredObjectByKey(ta.ESRNUMBER),
				(String) biller.getExtInfoStoredObjectByKey(ta.ESRSUB), rn.getRnId(), 27);
		qrReference = esr.makeRefNr(false);

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

	protected String getXmlVersion() {
		Attr attr = xmldom.getDocumentElement().getAttributeNode("xsi:schemaLocation"); //$NON-NLS-1$
		String location = null;
		if (attr != null) {
			location = attr.getValue();
			if (location.contains("InvoiceRequest_400")) {//$NON-NLS-1$
				return "4.0";//$NON-NLS-1$
			} else if (location.contains("InvoiceRequest_440")) {//$NON-NLS-1$
				return "4.4";//$NON-NLS-1$
			} else if (location.contains("InvoiceRequest_450")) {//$NON-NLS-1$
				return "4.5";//$NON-NLS-1$
			}
		}
		return StringUtils.EMPTY;
	}

}

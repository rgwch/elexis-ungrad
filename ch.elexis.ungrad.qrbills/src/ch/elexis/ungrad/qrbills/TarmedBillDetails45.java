package ch.elexis.ungrad.qrbills;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.base.ch.ebanking.esr.ESR;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IRnOutputter.TYPE;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.fd.invoice440.request.BalanceType;
import ch.fd.invoice440.request.BodyType;
import ch.fd.invoice440.request.InvoiceType;
import ch.fd.invoice440.request.ReminderType;
import ch.fd.invoice440.request.RequestType;
import ch.fd.invoice440.request.ServicesType;
import ch.fd.invoice440.request.TreatmentType;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import at.medevit.elexis.tarmed.model.TarmedJaxbUtil;
import at.medevit.elexis.tarmed.model.Constants;

public class TarmedBillDetails45 extends QRBillDetails {
	public static int FALL_UVG = 1;
	public static int FALL_IVG = 2;
	public static int FALL_KVG = 3;
	public static int FALL_MVG = 4;
	String outputDirPDF, outputDirXML;
	RequestType request;
	String paymentMode = XMLExporter.TIERS_PAYANT;
	String documentId;

	Kontakt guarantor;
	Kontakt zuweiser;
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
	BalanceType balance;
	Document xmldom;
	private static Logger logger = LoggerFactory.getLogger(TarmedBillDetails45.class);

	public TarmedBillDetails45(Rechnung rn, TYPE type, boolean bStrict) throws Exception {
		super(rn);
		this.type = type;
		outputDirPDF = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR_PDF, CoreHub.getTempDir().getAbsolutePath());
		outputDirXML = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR_XML, CoreHub.getTempDir().getAbsolutePath());
		if (rn.getInvoiceState() == InvoiceState.DEFECTIVE) {
			throw new Exception("Fehler in Rechnung " + rn.getNr());
		}
		File xmlfile = new File(outputDirXML, rn.getNr() + ".xml");
		xmldom = readDom(xmlfile);
		ch.fd.invoice450.request.RequestType request = TarmedJaxbUtil
				.unmarshalInvoiceRequest450(new FileInputStream(xmlfile));
		if (request == null) {
			logger.error("Could not unmarshall xml document for invoice");
			throw new Exception("Bad xml structure in " + rn.getNr());
		}
		ch.fd.invoice450.request.BodyType body = request.getPayload().getBody();
		services = body.getServices();
		ch.fd.invoice450.request.InvoiceType invoice = request.getPayload().getInvoice();
		TimeTool date = new TimeTool(invoice.getRequestDate().toString());
		documentId = invoice.getRequestId() + " - " + date.toString(TimeTool.DATE_GER) + " "
				+ date.toString(TimeTool.TIME_FULL);
		remarks = body.getRemark();

		paymentMode = XMLExporter.TIERS_PAYANT;
		if (body.getTiersGarant() != null) {
			paymentMode = XMLExporter.TIERS_GARANT;
		}
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

	private Document readDom(File xmlFile) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			return dbf.newDocumentBuilder().parse(VirtualFilesystemServiceHolder.get().of(xmlFile).openInputStream());
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LoggerFactory.getLogger(getClass()).error("Error parsing XML", e); //$NON-NLS-1$
		}
		return null;
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

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

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.arzttarife_schweiz.Messages;
import ch.elexis.base.ch.ebanking.esr.ESR;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IRnOutputter.TYPE;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.data.util.SortedList;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.ui.Hub;
import ch.elexis.data.Mandant;
import ch.elexis.tarmed.printer.Rn44Comparator;
import ch.elexis.tarmed.printer.XMLPrinterUtil;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.elexis.ungrad.Resolver;
import ch.fd.invoice440.request.*;
import ch.rgw.io.FileTool;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Create the Tarmed-page of the bill.
 * The format is defined here: https://www.forum-datenaustausch.ch/de/xml-standards-formulare/allgemein/xml-standards/
 * @author gerry
 *
 */
public class Tarmedprinter {
	
	private static Logger logger = LoggerFactory.getLogger(Tarmedprinter.class);
	
	private static final String FREETEXT = "freetext";
	private static final String BY_CONTRACT = "by_contract";
	private static final String SPACE = " ";
	
	private static double cmPerLine = 0.67; // Höhe pro Zeile (0.65 plus Toleranz)
	private static double cmFirstPage = 12.0; // Platz auf der ersten Seite
	private static double cmMiddlePage = 21.0; // Platz auf Folgeseiten
	private static double cmFooter = 4; // Platz für Endabrechnung
	private double cmAvail = 20.0; // Verfügbare Druckhöhe in cm
	
	private TimeTool tTime;
	private double sideTotal;
	private TarmedBillDetails billDetails;
	private Map<String, IPersistentObject> replacer;
	
	private static DecimalFormat df = new DecimalFormat(StringConstants.DOUBLE_ZERO);
	
	public Tarmedprinter(){
		tTime = new TimeTool();
		DecimalFormatSymbols custom = new DecimalFormatSymbols();
		custom.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(custom);
	}
	
	private String loadTemplate(String name) throws Exception{
		String filename = PlatformHelper.getBasePath("ch.elexis.ungrad.qrbills") + File.separator
			+ "rsc" + File.separator + name;
		File templatefile = new File(filename);
		if (!templatefile.exists()) {
			throw new Exception("Template not found: " + filename);
		}
		return FileTool.readTextFile(templatefile);
	}
	
	/**
	 * Create a HTML representation of the bill
	 * @param bill an earlyer created Meta-Object of the bill.
	 * @return a file handle to an HTML file (which can span multiple pages.
	 * @throws Exception
	 */
	public File print(TarmedBillDetails bill) throws Exception{
		
		File outfile = new File(bill.outputDirPDF, bill.rn.getNr() + "_rf.html");
		
		String currentPage = loadTemplate("tarmed44_page1.html");
		replacer = new HashMap<>();
		
		Mandant mSave = (Mandant) ElexisEventDispatcher.getSelected(Mandant.class);
		
		Hub.setMandant(bill.mandator);
		
		if (bill.type == TYPE.COPY) {
			currentPage = currentPage.replace("[F5]", Messages.RnPrintView_yes); //$NON-NLS-1$
		} else {
			currentPage = currentPage.replace("[F5]", Messages.RnPrintView_no); //$NON-NLS-1$
		}
		String gesetzDatum = "Falldatum";
		String gesetzNummer = "Fall-Nr.";
		String gesetzZSRNIF = "AHV-Nr.";
		if (bill.fallType == TarmedBillDetails.FALL_UVG) {
			gesetzDatum = "Unfalldatum";
			gesetzNummer = "Unfall-Nr.";
		}
		if (bill.fallType == TarmedBillDetails.FALL_IVG) {
			gesetzDatum = "Verfügungsdatum";
			gesetzNummer = "Verfügungs-Nr.";
			gesetzZSRNIF = "NIF-Nr.(P)";
		}
		String vekaNumber =
			StringTool.unNull((String) bill.fall.getExtInfoStoredObjectByKey("VEKANr"));
		
		currentPage =
			currentPage.replace("[F44.Datum]", gesetzDatum).replace("[F44.Nummer]", gesetzNummer);
		currentPage = currentPage.replace("[F44.FDatum]", bill.caseDate).replace("[F44.FNummer]",
			bill.caseNumber);
		currentPage =
			currentPage.replace("[F44.ZSRNIF]", gesetzZSRNIF).replace("[F44.VEKANr]", vekaNumber);
		
		currentPage = processHeaders(currentPage, bill, 1);
		currentPage = addDiagnoses(currentPage, bill.treatments);
		currentPage = addRemarks(currentPage, bill.remarks);
		currentPage = addReminderFields(currentPage, bill.reminders, bill.rn.getNr());
		
		if (bill.zuweiser != null) {
			replacer.put("Zuweiser", bill.zuweiser);
		}
		
		replacer.put("Biller", bill.biller);
		replacer.put("Provider", bill.mandator);
		replacer.put("Patient", bill.patient);
		replacer.put("Adressat", bill.adressat);
		replacer.put("Fall", bill.fall);
		Resolver resolver = new Resolver(replacer, true);
		currentPage = resolver.resolve(currentPage);
		// Remove all unreplaced fields
		currentPage = currentPage.replaceAll("\\[F.+\\]", ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		// ------------ Service records ---------
		List<Object> serviceRecords = bill.services.getRecordTarmedOrRecordDrgOrRecordLab();
		
		// lookup EAN numbers in services
		String[] eanArray = initEanArray(serviceRecords);
		HashMap<String, String> eanMap = XMLPrinterUtil.getEANHashMap(eanArray);
		currentPage = currentPage.replace("[F98]", XMLPrinterUtil.getEANList(eanArray));
		
		// add the various record services
		SortedList<Object> serviceRecordsSorted =
			new SortedList<Object>(serviceRecords, new Rn44Comparator());
		int pageNumber = 1;
		sideTotal = 0.0;
		cmAvail = cmFirstPage;
		StringBuilder sb = new StringBuilder();
		for (Object obj : serviceRecordsSorted) {
			if (obj instanceof RecordServiceType) {
				RecordServiceType rec = (RecordServiceType) obj;
				sb.append(getRecordServiceString(rec, eanMap));
				sb.append("<tr><td></td><td></td><td colspan=\"16\" class=\"text\">")
					.append(rec.getName()).append("</td></tr>");
			} else if (obj instanceof RecordTarmedType) {
				RecordTarmedType tarmed = (RecordTarmedType) obj;
				sb.append(getTarmedRecordString(tarmed, eanMap));
				String posname = tarmed.getName();
				sb.append("<tr><td></td><td></td><td colspan=\"15\" class=\"text\">")
					.append(posname).append("</td></tr>");
			}
			cmAvail -= cmPerLine;
			
			// End current page and begin new page
			if (cmAvail <= cmPerLine) {
				sb.append(
					"</tbody></table><p style=\"text-align:right;margin-right:15mm;font-size:8pt;\">Zwischentotal: ")
					.append("<b>").append(df.format(sideTotal)).append("</b></p>");
				sb.append("</div></div><p style=\"page-break-after: always;\"></p>"
					+ "<div style=\"position:relative;\">");
				// addESRCodeLine(balance, tcCode, esr);
				pageNumber += 1;
				cmAvail = cmMiddlePage;
				String page_n =
					processHeaders(loadTemplate("tarmed44_page_n.fragment"), bill, pageNumber);
				page_n = resolver.resolve(page_n);
				sb.append(page_n);
			}
		}
		// addESRCodeLine(balance, tcCode, esr);
		
		// --------------------------------------
		currentPage = currentPage.replace("[Leistungen]", sb.toString());
		double rest = Math.max(cmAvail - 1.0, 0.1);
		currentPage = createBalance(currentPage, bill);
		currentPage = currentPage.replace("[padding]", Long.toString(Math.round(rest * 10)));
		
		FileTool.writeTextFile(outfile, currentPage);
		Hub.setMandant(mSave);
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			// never mind
		}
		return outfile;
	}
	
	private String addReminderFields(String page, ReminderType reminder, String nr){
		String reminderDate = "";
		String reminderNr = "";
		
		if (reminder != null) {
			String reminderLevel = reminder.getReminderLevel();
			reminderNr = nr + "_m" + reminderLevel;
			
			DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			XMLGregorianCalendar date = reminder.getRequestDate();
			GregorianCalendar cal = date.toGregorianCalendar();
			reminderDate = df.format(cal.getTime());
		}
		page = page.replace("[F44.MDatum]", reminderDate);
		page = page.replace("[F44.MNr]", reminderNr);
		return page;
	}
	
	private String processHeaders(String page, final TarmedBillDetails billDetails,
		final int pagenumber){
		if (billDetails.paymentMode == XMLExporter.TIERS_GARANT) {
			page = page.replace("[Titel]", "Rückforderungsbeleg");
		} else {
			page = page.replace("[Titel]", "TP-Rechnung");
		}
		page = page.replace("[DocID]", billDetails.documentId).replace("[pagenr]",
			Integer.toString(pagenumber));
		if (billDetails.fallType == TarmedBillDetails.FALL_IVG) {
			page = page.replace("[NIF]", TarmedRequirements.getNIF(billDetails.mandator)); //$NON-NLS-1$
			String ahv = TarmedRequirements.getAHV(billDetails.patient);
			if (StringTool.isNothing(ahv)) {
				ahv = billDetails.fall.getRequiredString("AHV-Nummer");
			}
			page = page.replace("[F60]", ahv); //$NON-NLS-1$
		} else {
			page = page.replace("[NIF]", TarmedRequirements.getKSK(billDetails.mandator)); //$NON-NLS-1$
			page = page.replace("[F60]", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return page.replaceAll("\\?\\?\\??[a-zA-Z0-9 \\.]+\\?\\?\\??", "");
	}
	
	
	private String addRemarks(String page, final String remark){
		if (remark != null && !remark.isEmpty()) {
			page = page.replace("[remark]", remark);
		} else {
			page = page.replace("[remark]", "");
		}
		return page;
	}
	
	private void addESRCodeLine(TarmedBillDetails bill, String tcCode){
		String offenRp = bill.amountDue.getCentsAsString();
		if (tcCode != null) {
			ESR esr = new ESR(bill.biller.getInfoString(TarmedACL.getInstance().ESRNUMBER),
				bill.biller.getInfoString(TarmedACL.getInstance().ESRSUB), bill.rn.getRnId(),
				ESR.ESR27);
			esr.createCodeline(offenRp, tcCode);
			
		}
	}
	
	private String[] initEanArray(List<Object> serviceRecords){
		HashSet<String> eanUniqueSet = new HashSet<String>();
		
		for (Object record : serviceRecords) {
			String responsibleEAN = null;
			String providerEAN = null;
			
			if (record instanceof RecordServiceType) {
				RecordServiceType recService = (RecordServiceType) record;
				responsibleEAN = recService.getResponsibleId();
				providerEAN = recService.getProviderId();
			} else if (record instanceof RecordTarmedType) {
				RecordTarmedType recTarmed = (RecordTarmedType) record;
				responsibleEAN = recTarmed.getResponsibleId();
				providerEAN = recTarmed.getProviderId();
			}
			
			if (responsibleEAN != null && !responsibleEAN.isEmpty()) {
				eanUniqueSet.add(responsibleEAN);
			}
			
			if (providerEAN != null && !providerEAN.isEmpty()) {
				eanUniqueSet.add(providerEAN);
			}
		}
		
		return XMLPrinterUtil.getEANArray(eanUniqueSet);
	}
	
	private String getRecordServiceString(RecordServiceType rec, HashMap<String, String> eanMap){
		if (rec.getDateBegin() == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		tTime.set(rec.getDateBegin().toGregorianCalendar());
		sb.append("<tr><td class=\"ziffer\">").append(tTime.toString(TimeTool.DATE_GER)) //$NON-NLS-1$
			.append("</td>");
		sb.append("<td class=\"ziffer\">").append(getTarifType(rec)).append("</td>");//$NON-NLS-1$ //$NON-NLS-2$
		String code = rec.getCode();
		sb.append("<td class=\"ziffer\">").append(code).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		String refCode = SPACE;
		if (code.length() < 10) {
			refCode = rec.getRefCode();
			
		}
		sb.append("<td class=\"ziffer\">").append(refCode).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">").append(rec.getSession()).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td></td>");
		sb.append("<td class=\"ziffer\">").append(rec.getQuantity()).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// unit, scale factor, unit factor mt & tt
		sb.append("<td class=\"ziffer\">").append(SPACE).append("</td>"); //$NON-NLS-1$
		sb.append("<td class=\"ziffer\">").append(SPACE).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">").append(SPACE).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">").append(SPACE).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">").append(SPACE).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">").append(SPACE).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		
		String providerEAN = rec.getProviderId();
		String responsibleEAN = rec.getResponsibleId();
		if (getTarifType(rec) != null) {
			if (providerEAN != null && !providerEAN.isEmpty()) {
				sb.append("<td class=\"ziffer\">").append(eanMap.get(providerEAN) + "</td>");//$NON-NLS-1$
			}
			
			if (responsibleEAN != null && !responsibleEAN.isEmpty()) {
				sb.append("<td class=\"ziffer\">").append(eanMap.get(responsibleEAN) + "</td>"); //$NON-NLS-1$
			}
		} else {
			sb.append("<td class=\"ziffer\"></td>");
		}
		
		if (rec.isObligation()) {
			sb.append("<td class=\"ziffer\">1</td>"); //$NON-NLS-1$
		} else {
			sb.append("<td class=\"ziffer\">0</td>"); //$NON-NLS-1$
		}
		
		double amount = rec.getAmount();
		double vatRate = rec.getVatRate();
		sb.append("<td class=\"ziffer\">") //$NON-NLS-1$
			.append(Integer.toString(XMLPrinterUtil.guessVatCode(vatRate + ""))).append("</td>");
		sb.append("<td class=\"ziffer\">").append(df.format(amount));
		sideTotal += amount;
		sb.append("</td></tr>"); //$NON-NLS-1$
		
		return sb.toString();
	}
	
	private String getTarmedRecordString(RecordTarmedType tarmed, HashMap<String, String> eanMap){
		
		if (tarmed.getDateBegin() == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		tTime.set(tarmed.getDateBegin().toGregorianCalendar());
		sb.append("<tr><td class=\"ziffer\">").append(tTime.toString(TimeTool.DATE_GER))
			.append("</td>");
		sb.append("<td class=\"ziffer\">").append(tarmed.getTariffType()).append("</td>");
		sb.append("<td class=\"ziffer\">").append(tarmed.getCode()).append("</td>");
		String refCode = tarmed.getRefCode();
		if (refCode == null) {
			refCode = SPACE;
		}
		sb.append("<td class=\"ziffer\">").append(refCode).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">").append(tarmed.getSession()).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		
		String bodyLocation = tarmed.getBodyLocation();
		if (bodyLocation.startsWith("l")) { //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("<td class=\"ziffer\">L</td>");
		} else if (bodyLocation.startsWith("r")) { //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("<td class=\"ziffer\">R</td>");
		} else {
			sb.append("<td class=\"ziffer\"> </td>");
		}
		
		sb.append("<td class=\"ziffer\">").append(tarmed.getQuantity()).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">").append(tarmed.getUnitMt()).append("</td>"); //$NON-NLS-1$
		sb.append("<td class=\"ziffer\">").append(tarmed.getScaleFactorMt()).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">") //$NON-NLS-1$
			.append(roundDouble(tarmed.getUnitFactorMt() * tarmed.getExternalFactorMt())).append("</td>"); //$NON-NLS-1$
		
		sb.append("<td class=\"ziffer\">").append(tarmed.getUnitTt()).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">").append(tarmed.getScaleFactorTt()).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("<td class=\"ziffer\">").append(tarmed.getUnitFactorTt()).append("</td>"); //$NON-NLS-1$ //$NON-NLS-2$
		
		String providerEAN = tarmed.getProviderId();
		String responsibleEAN = tarmed.getResponsibleId();
		if (tarmed.getTariffType() != null) {
			if (providerEAN != null && !providerEAN.isEmpty()) {
				sb.append("<td class=\"ziffer\">").append(eanMap.get(providerEAN) + "</td>");//$NON-NLS-1$
			}
			
			if (responsibleEAN != null && !responsibleEAN.isEmpty()) {
				sb.append("<td class=\"ziffer\">").append(eanMap.get(responsibleEAN) + "</td>"); //$NON-NLS-1$
			}
		} else {
			sb.append("<td class=\"ziffer\"></td>");
		}
		
		if (tarmed.isObligation()) {
			sb.append("<td class=\"ziffer\">1</td>"); //$NON-NLS-1$
		} else {
			sb.append("<td class=\"ziffer\">0</td>"); //$NON-NLS-1$
		}
		
		double amount = tarmed.getAmount();
		double vatRate = tarmed.getVatRate();
		sb.append("<td class=\"ziffer\">") //$NON-NLS-1$
			.append(Integer.toString(XMLPrinterUtil.guessVatCode(vatRate + ""))).append("</td>");
		sb.append("<td class=\"ziffer\">").append(df.format(amount));
		sideTotal += amount;
		sb.append("</td></tr>"); //$NON-NLS-1$
		
		return sb.toString();
	}
	
	private double roundDouble(double value){
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	/*
	 * Create the final balance. Amount due is the amount of the bill minus prepayments plus charges for reminders.
	 */
	private String createBalance(String page, TarmedBillDetails bill){
		VatType vat = bill.balance.getVat();
		String vatNumber = vat.getVatNumber();
		if (vatNumber == null || vatNumber.equals(" ")) {
			vatNumber = "keine";
		} else {
			vatNumber = vatNumber + " MWST";
		}
		Money netto = new Money(bill.amountDue);
		Money prepaid = new Money(bill.amountPaid);
		prepaid.subtractMoney(bill.amountCharges);
		if (prepaid.isNegative()) {
			prepaid = new Money();
		}
		netto.subtractMoney(prepaid);
		netto.roundTo5();
		page = page.replace("[anzahlung]", df.format(prepaid));
		page = page.replace("[total]", df.format(bill.amountDue));
		page = page.replace("[MWST]", vatNumber);
		page = page.replace("[vat0]", df.format(getVatRate(0, vat)));
		page = page.replace("[vat0amount]", df.format(getVatAmount(0, vat)));
		page = page.replace("[vat0vat]", df.format(getVatVat(0, vat)));
		page = page.replace("[vat1]", df.format(getVatRate(1, vat)));
		page = page.replace("[vat1amount]", df.format(getVatAmount(1, vat)));
		page = page.replace("[vat1vat]", df.format(getVatVat(1, vat)));
		page = page.replace("[vat2]", df.format(getVatRate(2, vat)));
		page = page.replace("[vat2amount]", df.format(getVatAmount(2, vat)));
		page = page.replace("[vat2vat]", df.format(getVatVat(2, vat)));
		page = page.replace("[amountObl]", df.format(bill.balance.getAmountObligations()));
		page = page.replace("[amountdue]", df.format(netto));
		
		return page;
	}
	
	private String addDiagnoses(String page, TreatmentType treatment){
		if (treatment == null) {
			logger.debug("no treatments defined");
			return page;
		}
		
		List<DiagnosisType> diagnoses = treatment.getDiagnosis();
		if (diagnoses == null || diagnoses.isEmpty()) {
			logger.warn("No diagnoses found to print at the tarmed invoice request");
			return page;
		}
		
		List<String> occuredCodes = new ArrayList<String>();
		String type = "";
		String freetext = "";
		StringBuilder dCodesBuilder = new StringBuilder();
		for (DiagnosisType diagnose : diagnoses) {
			String dType = diagnose.getType();
			if (dType.equals(FREETEXT)) {
				freetext = diagnose.getValue();
				continue;
			}
			
			if (type.isEmpty()) {
				type = dType;
				dCodesBuilder.append(diagnose.getCode());
				occuredCodes.add(diagnose.getCode());
			} else if (type.equals(dType)) {
				// add each code only once
				if (!occuredCodes.contains(diagnose.getCode())) {
					dCodesBuilder.append("; "); //$NON-NLS-1$
					dCodesBuilder.append(diagnose.getCode());
					occuredCodes.add(diagnose.getCode());
				}
			}
		}
		
		if (type.equals(BY_CONTRACT)) {
			type = "TI-Code"; //$NON-NLS-1$
		}
		
		page = page.replace("[F51]", type); //$NON-NLS-1$
		page = page.replace("[F52]", dCodesBuilder.toString()); //$NON-NLS-1$
		page = page.replace("[F53]", freetext); //$NON-NLS-1$
		return page;
	}
	
	private String getTarifType(RecordServiceType rec){
		if (rec instanceof RecordOtherType) {
			RecordOtherType other = (RecordOtherType) rec;
			return other.getTariffType();
		} else if (rec instanceof RecordDrugType) {
			RecordDrugType drug = (RecordDrugType) rec;
			return drug.getTariffType();
		} else if (rec instanceof RecordDRGType) {
			RecordDRGType drg = (RecordDRGType) rec;
			return drg.getTariffType();
		} else if (rec instanceof RecordMigelType) {
			RecordMigelType migel = (RecordMigelType) rec;
			return migel.getTariffType();
		} else if (rec instanceof RecordLabType) {
			RecordLabType lab = (RecordLabType) rec;
			return lab.getTariffType();
		} else if (rec instanceof RecordParamedType) {
			RecordParamedType param = (RecordParamedType) rec;
			return param.getTariffType();
		}
		return "";
	}
	
	private double getVatAmount(int code, VatType vat){
		VatRateType vatRate = getVatRateElement(code, vat);
		if (vatRate != null) {
			return vatRate.getAmount();
		}
		return 0.0D;
	}
	
	private double getVatRate(int code, VatType vat){
		VatRateType vatRate = getVatRateElement(code, vat);
		if (vatRate != null) {
			return vatRate.getVatRate();
		}
		return 0.0D;
	}
	
	private double getVatVat(int code, VatType vat){
		VatRateType vatRate = getVatRateElement(code, vat);
		if (vatRate != null) {
			return vatRate.getVat();
		}
		return 0.0D;
	}
	
	private VatRateType getVatRateElement(int code, VatType vat){
		List<VatRateType> vatRates = vat.getVatRate();
		for (VatRateType vatRate : vatRates) {
			double rate = vatRate.getVatRate();
			int vatCode = XMLPrinterUtil.guessVatCode(rate + "");
			if (vatCode == code) {
				return vatRate;
			}
		}
		return null;
	}
	
}

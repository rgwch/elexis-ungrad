/*******************************************************************************
 * Copyright (c) 2018-2024 by G. Weirich
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

import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.arzttarife_schweiz.Messages;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.interfaces.ISticker;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.utils.PlatformHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Sticker;
import ch.elexis.data.Zahlung;
import ch.elexis.pdfBills.OutputterUtil;
import ch.elexis.pdfBills.QrRnOutputter;
import ch.elexis.ungrad.MailQueue;
import ch.elexis.ungrad.Mailer;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.pdf.Manager;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.StringTool;

/**
 * An Elexis-IRnOutputter for ISO 20022 conforming bills. Creates a Tarmed/4.5
 * conforming details page and a summary page with Swiss QR-conformant payment
 * slip. Both are created from html templates and ultimately converted to PDF.
 * 
 * @author gerry
 *
 */
public class QR_Outputter implements IRnOutputter {
	private Map<String, PersistentObject> replacer = new HashMap<>();
	Resolver resolver;
	private QR_SettingsControl qrs;
	private QR_Encoder qr;
	private Manager pdfManager;
	private boolean modifyInvoiceState;
	private IConfigService cfg = ConfigServiceHolder.get();
	String mailPref;
	Mailer mailer;

	private String[] shouldMail(TarmedBillDetails45 bill) {
		Kontakt k = bill.adressat;
		String mailaddr = "";
		String mailbody = cfg.get(PreferenceConstants.BY_MAIL_BODY, "");
		if (!StringTool.isNothing(mailPref)) {
			mailaddr = bill.fall.getInfoString(mailPref);
			if (!StringTool.isMailAddress(mailaddr)) {
				return null;
			}
			String altBody = bill.fall.getInfoString("Mailtext");
			if (!StringTool.isNothing(altBody)) {
				mailbody = altBody;
			}
			return new String[] { mailaddr, mailbody };
		}
		return null;
	}

	public QR_Outputter() {
		cfg = ConfigServiceHolder.get();
		mailPref = cfg.get(PreferenceConstants.BY_MAIL_IF_CASEVAR, "");
		mailer = new Mailer();
		mailer.showSuccess(false);
	}

	@Override
	public String getDescription() {
		return "Tarmedrechnung mit QR Code";
	}

	@Override
	public boolean canStorno(final Rechnung rn) {
		return false;
	}

	@Override
	public boolean canBill(final Fall fall) {
		return true;
	}

	@Override
	public Control createSettingsControl(final Object parent) {
		qrs = new QR_SettingsControl((Composite) parent);
		return qrs;
	}

	@Override
	public void saveComposite() {
		qrs.doSave();
	}

	/**
	 * To keep things easier, we ask the legacy outputter to produce the
	 * tarmedbill-page for us. After that, we do the QR-Page and the printing and
	 * mailing from here.
	 */
	@Override
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn, Properties props) {
		qr = new QR_Encoder();
		pdfManager = new Manager();
		modifyInvoiceState = true;
		// hack: save original config and set our paths. Then let the medeleix outputter
		// create the Tarmed-Form

		boolean legacy_useGlobalDirs = OutputterUtil.useGlobalOutputDirs();
		cfg.setLocal(OutputterUtil.CFG_PRINT_GLOBALOUTPUTDIRS, false);
		String legacy_root = QrRnOutputter.CFG_ROOT;
		String legacy_xmlDir = cfg.getLocal(legacy_root + QrRnOutputter.XMLDIR, "");
		String legacy_pdfDir = cfg.getLocal(legacy_root + QrRnOutputter.PDFDIR, "");

		String outputDirPDF = cfg.getLocal(PreferenceConstants.RNN_DIR_PDF, CoreHub.getTempDir().getAbsolutePath());
		String outputDirXML = cfg.getLocal(PreferenceConstants.RNN_DIR_XML, CoreHub.getTempDir().getAbsolutePath());
		cfg.setLocal(legacy_root + QrRnOutputter.XMLDIR, outputDirXML);
		cfg.setLocal(legacy_root + QrRnOutputter.PDFDIR, outputDirPDF);
		QrRnOutputter legacyOutputter = new QrRnOutputter();
		props.put(IRnOutputter.PROP_OUTPUT_NOUI, "true");
		props.put(IRnOutputter.PROP_OUTPUT_NOPRINT, "true");
		cfg.setLocal(QrRnOutputter.CFG_ROOT + QrRnOutputter.CFG_PRINT_BESR, false);
		cfg.setLocal(QrRnOutputter.CFG_ROOT + QrRnOutputter.CFG_PRINT_RF, true);

		Result<Rechnung> result = legacyOutputter.doOutput(type, rnn, props);
		// reset original config
		cfg.setLocal(legacy_root + QrRnOutputter.XMLDIR, legacy_xmlDir);
		cfg.setLocal(legacy_root + QrRnOutputter.PDFDIR, legacy_pdfDir);
		cfg.setLocal(OutputterUtil.CFG_PRINT_GLOBALOUTPUTDIRS, legacy_useGlobalDirs);

		if (result.isOK()) {
			// If tarmed-forms are ok, create QR-Forms and print both (doPrint)
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			try {
				progressService.runInUI(PlatformUI.getWorkbench().getProgressService(), new IRunnableWithProgress() {

					@Override
					public void run(final IProgressMonitor monitor) {
						monitor.beginTask("Drucke Rechnungen", rnn.size() * 3);
						for (Rechnung rn : rnn) {
							doPrint(rn, monitor, type, result);
							if (monitor.isCanceled()) {
								break;
							}
						}
						monitor.done();

					}
				}, null);

			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
		}
		if (result.isOK()) {
			SWTHelper.showInfo("Ausgabe beendet", rnn.size() + " QR-Rechnung(en) wurde(n) ausgegeben");
		} else {
			SWTHelper.showError("QR-Output", "Fehler bei der Rechnungsausgabe", result.toString()
					+ "\nSie können die fehlerhaften Rechnungen mit Status fehlerhaft in der Rechnungsliste anzeigen und korrigieren");

		}

		return result;
	}

	private void doPrint(final Rechnung rn, final IProgressMonitor monitor, final TYPE type, Result<Rechnung> res) {
		try {
			monitor.subTask(rn.getNr() + " wird ausgegeben");
			TarmedBillDetails45 bill = new TarmedBillDetails45(rn, type,
					LocalConfigService.get(PreferenceConstants.MISSING_DATA, true));
			// the rfFile was already created at this point (by doOutput-legacyOutputter).
			// We just reference it by name here
			File rfFile = new File(bill.outputDirPDF, bill.rn.getNr() + "_rf.pdf");
			File qrFile = outputQRPage(bill);
			String[] mailing = shouldMail(bill);
			if (mailing != null) {
				String subject = resolver.resolve(cfg.get(PreferenceConstants.BY_MAIL_SUBJECT, "Rechnung"));
				String body = resolver.resolve(mailing[1]);
				mailer.defaultMail(mailing[0], subject, body,
						new String[] { rfFile.getAbsolutePath(), qrFile.getAbsolutePath() });
				rn.addTrace(Rechnung.OUTPUT, "by Mail an " + mailing[0]);
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {

				}
				monitor.worked(1);
			} else {
				if (LocalConfigService.get(PreferenceConstants.FACE_DOWN, false)) {
					monitor.worked(1);
					sendToPrinter(qrFile);
					if (LocalConfigService.get(PreferenceConstants.PRINT_TARMED, true)) {
						sendToPrinter(rfFile);
					} else {
						rfFile.delete();
					}
				} else {
					if (LocalConfigService.get(PreferenceConstants.PRINT_TARMED, false)) {
						sendToPrinter(rfFile);
					} else {
						rfFile.delete();
					}
					monitor.worked(1);
					sendToPrinter(qrFile);
				}
			}
			res.add(new Result<Rechnung>(rn));
			monitor.worked(1);

		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(new Result<Rechnung>(SEVERITY.ERROR, 2, ex.getMessage(), rn, true));
		}

	}

	private File outputQRPage(final TarmedBillDetails45 bill) throws Exception {
		File pdfFile = null;
		if (LocalConfigService.get(PreferenceConstants.PRINT_QR, true)) {
			String default_template = PlatformHelper.getBasePath("ch.elexis.ungrad.qrbills") + File.separator + "rsc"
					+ File.separator + "qrbill_template_v5.html";
			String fname = "";
			switch (bill.rn.getInvoiceState()) {
			case OPEN:
			case OPEN_AND_PRINTED:
				fname = cfg.get(PreferenceConstants.TEMPLATE_BILL, "");
				break;
			case DEMAND_NOTE_1:
			case DEMAND_NOTE_1_PRINTED:
				fname = cfg.get(PreferenceConstants.TEMPLATE_REMINDER1, "");
				break;
			case DEMAND_NOTE_2:
			case DEMAND_NOTE_2_PRINTED:
				fname = cfg.get(PreferenceConstants.TEMPLATE_REMINDER2, "");
				break;
			case DEMAND_NOTE_3:
			case DEMAND_NOTE_3_PRINTED:
				fname = cfg.get(PreferenceConstants.TEMPLATE_REMINDER3, "");
				break;
			default:
				fname = default_template;
			}
			File template = new File(fname);
			if (!template.exists()) {
				template = new File(default_template);
			}
			String rawHTML = FileTool.readTextFile(template);

			replacer.put("Adressat", bill.adressat);
			replacer.put("Mandant", bill.biller);
			replacer.put("Patient", bill.patient);
			replacer.put("Rechnung", bill.rn);
			resolver = new Resolver(replacer, true);

			String cookedHTML = resolver.resolve(rawHTML);
			byte[] png = qr.generate(bill);
			File imgFile = new File(bill.outputDirPDF, bill.rn.getRnId() + ".png");
			FileTool.writeFile(imgFile, png);

			StringBuilder sbSummary = new StringBuilder();
			sbSummary.append("<table style=\"width:100%\">");
			if (!bill.amountTarmed.isNeglectable()) {
				sbSummary.append("<tr><td>").append(Messages.RnPrintView_tarmedPoints)
						.append("</td><td class=\"amount\">").append(bill.amountTarmed.getAmountAsString())
						.append("</td></tr>");
			}
			if (!bill.amountDrug.isNeglectable()) {
				sbSummary.append("<tr><td>").append(Messages.RnPrintView_medicaments)
						.append("</td><td class=\"amount\">").append(bill.amountDrug.getAmountAsString())
						.append("</td></tr>");
			}
			if (!bill.amountLab.isNeglectable()) {
				sbSummary.append("<tr><td>").append(Messages.RnPrintView_labpoints).append("</td><td class=\"amount\">")
						.append(bill.amountLab.getAmountAsString()).append("</td></tr>");
			}
			if (!bill.amountMigel.isNeglectable()) {
				sbSummary.append("<tr><td>").append(Messages.RnPrintView_migelpoints)
						.append("</td><td class=\"amount\">").append(bill.amountMigel.getAmountAsString())
						.append("</td></tr>");
			}
			if (!bill.amountPhysio.isNeglectable()) {
				sbSummary.append("<tr><td>").append(Messages.RnPrintView_physiopoints)
						.append("</td><td class=\"amount\">").append(bill.amountPhysio.getAmountAsString())
						.append("</td></tr>");
			}
			if (!bill.amountUnclassified.isNeglectable()) {
				sbSummary.append("<tr><td>").append("Diverse Nicht-Pflichleistungen:")
						.append("</td><td class=\"amount\">").append(bill.amountUnclassified.getAmountAsString())
						.append("</td></tr>");
			}
			for (Zahlung z : bill.charges) {
				Money betrag = new Money(z.getBetrag()).multiply(-1.0);
				sbSummary.append("<tr><td>").append(z.getBemerkung()).append(":</td><td class=\"amount\">")
						.append(betrag.getAmountAsString()).append("</td></tr>");
			}
			if (!bill.amountPaid.isNeglectable()) {
				sbSummary.append("<tr><td>Angezahlt:</td><td class=\"amount\">")
						.append("-" + bill.amountPaid.getAmountAsString()).append("</td></tr>");
			}
			sbSummary.append("</table>");
			String finished = cookedHTML.replace("[QRIMG]", bill.rn.getRnId() + ".png")
					.replace("[LEISTUNGEN]", sbSummary.toString()).replace("[CURRENCY]", bill.currency)
					.replace("[AMOUNT]", bill.amountTotalWithCharges.getAmountAsString())
					.replace("[IBAN]", bill.getFormatted(bill.qrIBAN))
					.replace("[BILLER]", bill.combinedAddress(bill.biller))
					.replace("[ESRLINE]", bill.getFormatted(bill.qrReference))
					.replace("[INFO]", Integer.toString(bill.numCons) + " Konsultationen")
					.replace("[ADDRESSEE]", bill.combinedAddress(bill.adressat)).replace("[DUE]", bill.dateDue);

			File htmlFile = new File(bill.outputDirPDF, bill.rn.getNr() + ".html");
			pdfFile = new File(bill.outputDirPDF, bill.rn.getNr() + "_qr.pdf");
			FileTool.writeTextFile(htmlFile, finished);
			bill.writePDF(htmlFile, pdfFile);
			// sendToPrinter(pdfFile);
			imgFile.delete();
			if (!cfg.getLocal(PreferenceConstants.DEBUGFILES, false)) {
				htmlFile.delete();
			}
		}
		return pdfFile;
	}

	private void sendToPrinter(File pdfFile) throws PrinterException, IOException {
		if (cfg.getLocal(PreferenceConstants.DO_PRINT, false)) {
			String defaultPrinter = null;
			if (cfg.getLocal(PreferenceConstants.DIRECT_PRINT, false)) {
				defaultPrinter = cfg.getLocal(PreferenceConstants.DEFAULT_PRINTER, "");
			}
			if (pdfManager.printFromPDF(pdfFile, defaultPrinter)) {
				if (cfg.getLocal(PreferenceConstants.DELETE_AFTER_PRINT, false)) {
					pdfFile.delete();
				}
			}
		}

	}

}

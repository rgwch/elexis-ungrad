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

import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.jdom.Document;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.elexis.views.RnPrintView2;
import ch.rgw.crypt.BadParameterException;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

/**
 * An Elexis-IRnOutputter for ISO 20022 conformant bills. Creates a Tarmed/4.4
 * conformant details page and a summary page with Sqiss QR-conformant payment
 * slip. Both are created from html templates and ultimately converted to PDF.
 * 
 * @author gerry
 *
 */
public class QR_Outputter implements IRnOutputter {
	Map<String, IPersistentObject> replacer = new HashMap<>();
	QR_SettingsControl qrs;
	// IWorkbenchPage rnPage;
	// RnPrintViewQR rnp;
	XMLExporter xmlex;
	TarmedACL ta = TarmedACL.getInstance();
	QR_Encoder qr;
	QR_Printer printer;
	private boolean modifyInvoiceState;

	public QR_Outputter() {
	}

	@Override
	public String getDescription() {
		return "Rechnung mit QR Code";
	}

	@Override
	public boolean canStorno(Rechnung rn) {
		return false;
	}

	@Override
	public boolean canBill(Fall fall) {
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

	@SuppressWarnings("deprecation")
	@Override
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn, Properties props) {
		Result<Rechnung> res = new Result<Rechnung>();
		qr = new QR_Encoder();
		printer = new QR_Printer();
		xmlex = new XMLExporter();
		modifyInvoiceState = true;

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.runInUI(PlatformUI.getWorkbench().getProgressService(), new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor monitor) {
					monitor.beginTask("Drucke Rechnungen", rnn.size() * 2);
					for (Rechnung rn : rnn) {
						doPrint(rn, monitor, type, res);
						if (modifyInvoiceState) {
							int status_vorher = rn.getStatus();
							if ((status_vorher == RnStatus.OFFEN) || (status_vorher == RnStatus.MAHNUNG_1)
									|| (status_vorher == RnStatus.MAHNUNG_2) || (status_vorher == RnStatus.MAHNUNG_3)) {
								rn.setStatus(status_vorher + 1);
							}
							rn.addTrace(Rechnung.OUTPUT, getDescription() + ": " //$NON-NLS-1$
									+ RnStatus.getStatusText(rn.getStatus()));
						}
					}

				}
			}, null);

		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(new Result<Rechnung>(SEVERITY.ERROR, 1, ex.getMessage(), null, true));
		}
		if (res.isOK()) {
			SWTHelper.showInfo("Ausgabe beendet", rnn.size() + " QR-Rechnung(en) wurde(n) ausgegeben");
		} else {
			SWTHelper.showError("QR-Output", "Fehler bei der Rechnungsausgabe", res.toString());

		}
		return res;
	}

	private void doPrint(Rechnung rn, IProgressMonitor monitor, TYPE type, Result<Rechnung> res) {
		try {
			monitor.subTask(rn.getNr() + " wird ausgegeben");
			BillDetails bill = new BillDetails(rn, type);
			if (CoreHub.localCfg.get(PreferenceConstants.FACE_DOWN, false)) {
				printQRPage(bill);
				monitor.worked(1);
				printDetails(bill);
			} else {
				printDetails(bill);
				monitor.worked(1);
				printQRPage(bill);
			}
			res.add(new Result<Rechnung>(rn));
			monitor.worked(1);

		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(new Result<Rechnung>(SEVERITY.ERROR, 2, ex.getMessage(), rn, true));
		}
	}

	private void printQRPage(BillDetails bill) throws IOException, Exception, BadParameterException,
			UnsupportedEncodingException, FileNotFoundException, PrinterException {
		if (CoreHub.localCfg.get(PreferenceConstants.PRINT_QR, true)) {
			String default_template = PlatformHelper.getBasePath("ch.elexis.ungrad.qrbills") + File.separator + "rsc"
					+ File.separator + "qrbill_template_v4.html";
			String fname = "";
			switch (bill.rn.getStatus()) {
			case RnStatus.OFFEN:
			case RnStatus.OFFEN_UND_GEDRUCKT:
				fname = CoreHub.globalCfg.get(PreferenceConstants.TEMPLATE_BILL, "");
				break;
			case RnStatus.MAHNUNG_1:
			case RnStatus.MAHNUNG_1_GEDRUCKT:
				fname = CoreHub.globalCfg.get(PreferenceConstants.TEMPLATE_REMINDER1, "");
				break;
			case RnStatus.MAHNUNG_2:
			case RnStatus.MAHNUNG_2_GEDRUCKT:
				fname = CoreHub.globalCfg.get(PreferenceConstants.TEMPLATE_REMINDER2, "");
				break;
			case RnStatus.MAHNUNG_3:
			case RnStatus.MAHNUNG_3_GEDRUCKT:
				fname = CoreHub.globalCfg.get(PreferenceConstants.TEMPLATE_REMINDER3, "");
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
			Resolver resolver = new Resolver(replacer, true);

			String cookedHTML = resolver.resolve(rawHTML);
			byte[] png = qr.generate(bill);
			File imgFile = new File(bill.outputDirPDF, bill.rn.getRnId() + ".png");
			FileTool.writeFile(imgFile, png);

			String finished = cookedHTML.replace("[QRIMG]", bill.rn.getRnId() + ".png").replace("[CURRENCY]", bill.currency)
					.replace("[AMOUNT]", bill.amountDue.getAmountAsString()).replace("[IBAN]", bill.formattedIban)
					.replace("[BILLER]", bill.combinedAddress(bill.biller))
					.replace("[ESRLINE]", bill.formattedReference)
					.replace("[INFO]", Integer.toString(bill.numCons) + " Konsultationen")
					.replace("[ADDRESSEE]", bill.combinedAddress(bill.adressat)).replace("[DUE]", bill.dateDue);

			File htmlFile = new File(bill.outputDirPDF, bill.rn.getNr() + ".html");
			File pdfFile = new File(bill.outputDirPDF, bill.rn.getNr() + "_qr.pdf");
			FileTool.writeTextFile(htmlFile, finished);
			FileOutputStream fout = new FileOutputStream(pdfFile);
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withFile(htmlFile);
			builder.toStream(fout);
			builder.run();
			if (CoreHub.localCfg.get(PreferenceConstants.DO_PRINT, false)) {
				String defaultPrinter = null;
				if (CoreHub.localCfg.get(PreferenceConstants.DIRECT_PRINT, false)) {
					defaultPrinter = CoreHub.localCfg.get(PreferenceConstants.DEFAULT_PRINTER, "");
				}
				if (printer.print(pdfFile, defaultPrinter)) {
					if (CoreHub.localCfg.get(PreferenceConstants.DELETE_AFTER_PRINT, false)) {
						pdfFile.delete();
					}
				}
			}
			imgFile.delete();
			if (!CoreHub.localCfg.get(PreferenceConstants.DEBUGFILES, false)) {
				htmlFile.delete();
			}

		}
	}

	private void printDetails(BillDetails bill)
			throws Exception, FileNotFoundException, IOException, PrinterException {
		Tarmedprinter tp = new Tarmedprinter();
		if (CoreHub.localCfg.get(PreferenceConstants.PRINT_TARMED, true)) {

			File rfhtml = tp.print(bill);
			File pdfout = new File(bill.outputDirPDF, bill.rn.getNr() + "_rf.pdf");
			FileOutputStream rfpdf = new FileOutputStream(pdfout);
			PdfRendererBuilder builder = new PdfRendererBuilder();

			builder.useFastMode().withFile(rfhtml).toStream(rfpdf).run();
			if (CoreHub.localCfg.get(PreferenceConstants.DO_PRINT, false)) {
				String defaultPrinter = null;
				if (CoreHub.localCfg.get(PreferenceConstants.DIRECT_PRINT, false)) {
					defaultPrinter = CoreHub.localCfg.get(PreferenceConstants.DEFAULT_PRINTER, "");
				}
				if (printer.print(pdfout, defaultPrinter)) {
					if (CoreHub.localCfg.get(PreferenceConstants.DELETE_AFTER_PRINT, false)) {
						pdfout.delete();
					}
				}
			}

			if (!CoreHub.localCfg.get(PreferenceConstants.DEBUGFILES, false)) {
				rfhtml.delete();
			}
		}
	}

}

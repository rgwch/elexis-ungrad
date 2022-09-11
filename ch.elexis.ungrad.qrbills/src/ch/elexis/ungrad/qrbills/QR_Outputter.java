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
import java.io.FileOutputStream;
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
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.elexis.ungrad.qrbills.views.RnPrintViewQR;
import ch.elexis.views.RnPrintView2;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

/**
 * An Elexis-IRnOutputter for ISO 20022 conformant bills. Embeds a QR Code in a
 * HTML template and writes the result in a html file. (To be converted to pdf
 * or to print directly, whatever).
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

		// rnPage =
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			// rnp = (RnPrintViewQR) rnPage.showView(RnPrintViewQR.ID);

			progressService.runInUI(PlatformUI.getWorkbench().getProgressService(), new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor monitor) {
					monitor.beginTask("Drucke Rechnungen", rnn.size() * 10);
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

	public void doPrint(Rechnung rn, IProgressMonitor monitor, TYPE type, Result<Rechnung> res) {
		try {
			monitor.subTask(rn.getNr());
			String default_template = PlatformHelper.getBasePath("ch.elexis.ungrad.qrbills") + File.separator + "rsc"
					+ File.separator + "qrbill_template_v4.html";
			String fname = "";
			String outputDir = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR,
					CoreHub.getTempDir().getAbsolutePath());
			switch (rn.getStatus()) {
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

			BillDetails bill = new BillDetails(rn);
			replacer.put("Adressat", bill.adressat);
			replacer.put("Mandant", bill.biller);
			replacer.put("Patient", bill.patient);
			replacer.put("Rechnung", rn);
			Resolver resolver = new Resolver(replacer, true);

			String cookedHTML = resolver.resolve(rawHTML);
			byte[] png = qr.generate(rn, bill);
			File imgFile = new File(outputDir, rn.getRnId() + ".png");
			FileTool.writeFile(imgFile, png);

			String finished = cookedHTML.replace("[QRIMG]", rn.getRnId() + ".png").replace("[CURRENCY]", bill.currency)
					.replace("[AMOUNT]", bill.amount.getAmountAsString()).replace("[IBAN]", bill.formattedIban)
					.replace("[BILLER]", bill.combinedAddress(bill.biller))
					.replace("[ESRLINE]", bill.formattedReference)
					.replace("[INFO]", Integer.toString(bill.numCons) + " Konsultationen")
					.replace("[ADDRESSEE]", bill.combinedAddress(bill.adressat)).replace("[DUE]", bill.dateDue);

			File file = new File(outputDir, rn.getRnId() + ".html");
			File pdfFile = new File(outputDir, rn.getRnId() + ".pdf");
			FileTool.writeTextFile(file, finished);
			FileOutputStream fout = new FileOutputStream(pdfFile);
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withFile(file);
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
			file.delete();

			Tarmedprinter tp = new Tarmedprinter();
			File xmlfile = new File(outputDir, rn.getRnId() + ".xml");
			Document doc = xmlex.doExport(rn, xmlfile.getAbsolutePath(), type, true);
			tp.print(rn, doc, type, monitor);

			monitor.worked(5);
			// rnp.doPrint(rn, type, , monitor);
			res.add(new Result<Rechnung>(rn));

		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(new Result<Rechnung>(SEVERITY.ERROR, 2, ex.getMessage(), rn, true));
		}
	}

}

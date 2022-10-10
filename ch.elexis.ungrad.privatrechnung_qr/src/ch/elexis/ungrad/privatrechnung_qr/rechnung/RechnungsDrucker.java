/*******************************************************************************
 * Copyright (c) 2007-2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.privatrechnung_qr.rechnung;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.*;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.privatrechnung_qr.data.PreferenceConstants;
import ch.elexis.ungrad.qrbills.PDF_Printer;
import ch.elexis.ungrad.qrbills.QRBillDetails;
import ch.elexis.ungrad.qrbills.QR_Encoder;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.TimeTool;

public class RechnungsDrucker implements IRnOutputter {
	private QR_SettingsControl qrs;
	private QR_Encoder qr = new QR_Encoder();
	private PDF_Printer printer = new PDF_Printer();
	private Map<String, IPersistentObject> replacer = new HashMap<>();

	public String getDescription() {
		return "Privatrechnung QR PDF";
	}

	/**
	 * We'll take all sorts of bills
	 */
	public boolean canBill(final Fall fall) {
		return true;
	}

	/**
	 * We never storno
	 */
	public boolean canStorno(final Rechnung rn) {
		return false;
	}

	/**
	 * Create the Control that will be presented to the user before selecting the
	 * bill output target. Here we simply chose a template to use for the bill. In
	 * fact we need two templates: a template for the page with summary and giro and
	 * a template for the other pages
	 */
	public Object createSettingsControl(Object parent) {
		qrs = new QR_SettingsControl((Composite) parent);
		return qrs;
	}

	public void saveComposite() {
		qrs.doSave();
	}

	/**
	 * Print the bill(s)
	 */
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn, final Properties props) {
		final Result<Rechnung> result = new Result<Rechnung>();

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.runInUI(PlatformUI.getWorkbench().getProgressService(), new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor monitor) {
					monitor.beginTask("Drucke Rechnungen", rnn.size() * 3);
					for (Rechnung rn : rnn) {
						try {
							result.add(doPrint(rn, monitor, type));
							int status_vorher = rn.getStatus();
							if ((status_vorher == RnStatus.OFFEN) || (status_vorher == RnStatus.MAHNUNG_1)
									|| (status_vorher == RnStatus.MAHNUNG_2) || (status_vorher == RnStatus.MAHNUNG_3)) {
								rn.setStatus(status_vorher + 1);
							}
							rn.addTrace(Rechnung.OUTPUT, getDescription() + ": " //$NON-NLS-1$
									+ RnStatus.getStatusText(rn.getStatus()));
							monitor.worked(1);
							try {
								TimeUnit.MILLISECONDS.sleep(100);
							} catch (InterruptedException e) {
								break;
							}
						} catch (Exception ex) {
							ExHandler.handle(ex);
							result.add(SEVERITY.WARNING, 5, "Output error: " + ex.getMessage(), rn, true);
						}
					}
					monitor.done();
				}
			}, null);

		} catch (Exception ex) {
			ExHandler.handle(ex);
			result.add(Result.SEVERITY.ERROR, 1, ex.getMessage(), null, true);
			ErrorDialog.openError(null, "Exception", "Exception", ResultAdapter.getResultAsStatus(result));
			return result;
		}
		if (result.isOK()) {
			SWTHelper.showInfo("Ausgabe beendet", rnn.size() + " QR-Rechnung(en) wurde(n) ausgegeben");
		} else {
			SWTHelper.showError("QR-Output", "Fehler bei der Rechnungsausgabe", result.toString()
					+ "\nSie können die fehlerhaften Rechnungen mit Status fehlerhaft in der Rechnungsliste anzeigen und korrigieren");

		}
		return result;
	}

	private Result<Rechnung> doPrint(final Rechnung rn, final IProgressMonitor monitor, TYPE type) throws Exception {
		String default_template = PlatformHelper.getBasePath("ch.elexis.ungrad.privatrechnung_qr") + File.separator
				+ "rsc" + File.separator + "qrbill_template_p1.html";
		String fname = "";
		String mid = "/" + rn.getMandant().getId();
		switch (rn.getStatus()) {
		case RnStatus.OFFEN:
		case RnStatus.OFFEN_UND_GEDRUCKT:
			fname = CoreHub.localCfg.get(PreferenceConstants.TEMPLATE_BILL + mid, "");
			break;
		case RnStatus.MAHNUNG_1:
		case RnStatus.MAHNUNG_1_GEDRUCKT:
			fname = CoreHub.localCfg.get(PreferenceConstants.TEMPLATE_REMINDER1 + mid, "");
			break;
		case RnStatus.MAHNUNG_2:
		case RnStatus.MAHNUNG_2_GEDRUCKT:
			fname = CoreHub.localCfg.get(PreferenceConstants.TEMPLATE_REMINDER2 + mid, "");
			break;
		case RnStatus.MAHNUNG_3:
		case RnStatus.MAHNUNG_3_GEDRUCKT:
			fname = CoreHub.localCfg.get(PreferenceConstants.TEMPLATE_REMINDER3 + mid, "");
			break;
		default:
			fname = default_template;
		}
		File template = new File(fname);
		if (!template.exists()) {
			template = new File(default_template);
		}
		String rawHTML = FileTool.readTextFile(template);

		Result<Rechnung> ret = new Result<Rechnung>();
		QRBillDetails bill = new QRBillDetails(rn);
		bill.qrIBAN = CoreHub.globalCfg.get(PreferenceConstants.QRIBAN + "/" + rn.getMandant().getId(), "0");
		bill.qrReference = bill.createQRReference(
				CoreHub.globalCfg.get(PreferenceConstants.bankClient + "/" + rn.getMandant().getId(), "0"));
		ElexisEventDispatcher.fireSelectionEvent(bill.fall);
		replacer.put("Adressat", bill.adressat);
		replacer.put("Mandant", bill.biller);
		replacer.put("Rechnung", rn);
		Resolver resolver = new Resolver(replacer, true);
		String cookedHtml = resolver.resolve(rawHTML);
		List<Konsultation> kons = rn.getKonsultationen();
		Collections.sort(kons, new Comparator<Konsultation>() {
			TimeTool t0 = new TimeTool();
			TimeTool t1 = new TimeTool();

			public int compare(final Konsultation arg0, final Konsultation arg1) {
				t0.set(arg0.getDatum());
				t1.set(arg1.getDatum());
				return t0.compareTo(t1);
			}

		});
		// Leistungen und Artikel gruppieren
		Money sum = new Money();
		StringBuilder sb = new StringBuilder();
		HashMap<String, List<Verrechnet>> groups = new HashMap<String, List<Verrechnet>>();
		for (Konsultation k : kons) {
			String datum = new TimeTool(k.getDatum()).toString(TimeTool.DATE_GER);
			List<Verrechnet> vv = k.getLeistungen();
			for (Verrechnet v : vv) {
				Money mSingle = v.getNettoPreis();
				Money mLine = new Money(mSingle);
				mLine.multiply(v.getZahl());
				sum.addMoney(mLine);
				sb.append("<tr><td>").append(datum).append("</td><td style=\"padding-left:5mm;padding-right:5mm;\">")
						.append(v.getLabel()).append("</td><td class=\"amount\">").append(mSingle.getAmountAsString())
						.append("</td><td style=\"text-align:center\">").append(v.getZahl())
						.append("</td><td class=\"amount\">").append(mLine.getAmountAsString()).append("</td></tr>");

				IVerrechenbar iv = v.getVerrechenbar();
				if (iv != null) {
					String csName = iv.getCodeSystemName();
					List<Verrechnet> gl = groups.get(csName);
					if (gl == null) {
						gl = new ArrayList<Verrechnet>();
						groups.put(csName, gl);
					}
					gl.add(v);
				}
			}
		}
		sb.append("<tr><td  style=\"padding-top:3mm;\" colspan=\"4\">Total</td><td class=\"amount\">")
				.append(sum.getAmountAsString()).append("</td></tr>");

		bill.amountDue = sum;
		bill.addCharges();
		byte[] png = qr.generate(bill);
		File pdfDir = new File(CoreHub.localCfg.get(PreferenceConstants.RNN_DIR_PDF, ""));
		File imgFile = new File(pdfDir, rn.getNr() + ".png");
		FileTool.writeFile(imgFile, png);
		String finished = cookedHtml.replace("[QRIMG]", bill.rn.getNr() + ".png").replace("[LEISTUNGEN]", sb.toString())
				.replace("[CURRENCY]", bill.currency)
				.replace("[AMOUNT]", bill.amountTotalWithCharges.getAmountAsString())
				.replace("[IBAN]", bill.getFormatted(bill.qrIBAN))
				.replace("[BILLER]", bill.combinedAddress(bill.biller))
				.replace("[ESRLINE]", bill.getFormatted(bill.qrReference))
				.replace("[ADDRESSEE]", bill.combinedAddress(bill.adressat)).replace("[DUE]", bill.dateDue);

		File htmlFile = new File(pdfDir, bill.rn.getNr() + ".html");
		File pdfFile = new File(pdfDir, bill.rn.getNr() + "_qr.pdf");
		FileTool.writeTextFile(htmlFile, finished);
		FileOutputStream fout = new FileOutputStream(pdfFile);
		bill.writePDF(htmlFile, pdfFile);
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

		return ret;
	}
}

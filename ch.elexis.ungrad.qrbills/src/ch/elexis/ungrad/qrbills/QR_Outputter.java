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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

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
	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn, Properties props) {
		Result<Rechnung> res = new Result<Rechnung>();
		// QR_Generator qr = new QR_Generator();
		QR_Encoder qr = new QR_Encoder();
		QR_Printer printer = new QR_Printer();

		String default_template = PlatformHelper.getBasePath("ch.elexis.ungrad.qrbills") + File.separator + "rsc"
				+ File.separator + "qrbill_template_v4.html";

		try {

			for (Rechnung rn : rnn) {

				try {
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

					String finished = cookedHTML.replace("[QRIMG]", rn.getRnId() + ".png")
							.replace("[CURRENCY]", bill.currency).replace("[AMOUNT]", bill.amount.getAmountAsString())
							.replace("[IBAN]", bill.formattedIban)
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
					tp.print(rn, new File(outputDir,rn.getRnId()+".xml"),IRnOutputter.TYPE.ORIG);
					res.add(new Result<Rechnung>(rn));
				} catch (Exception ex) {
					ExHandler.handle(ex);
					res.add(new Result<Rechnung>(SEVERITY.ERROR, 2, ex.getMessage(), rn, true));
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(new Result<Rechnung>(SEVERITY.ERROR, 1, "Could  not find templateFile ", null, true));
		}
		if (res.isOK()) {
			SWTHelper.showInfo("Ausgabe beendet", rnn.size() + " QR-Rechnung(en) wurde(n) ausgegeben");
		} else {
			SWTHelper.showError("QR-Output", "Fehler bei der Rechnungsausgabe", res.toString());

		}
		return res;
	}

}

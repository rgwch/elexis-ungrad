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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.preferences.CorePreferenceInitializer;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Rechnung;
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
	String outputDir;
	Map<String, IPersistentObject> replacer = new HashMap<>();
	Text txPostProcess;
	String postProcessCommand = "";

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
		final Composite parentInc = (Composite) parent;
		Composite ret = new Composite(parentInc, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		Label l = new Label(ret, SWT.NONE);
		l.setText(Messages.XMLExporter_PleaseEnterOutputDirectoryForBills);
		l.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		final Text text = new Text(ret, SWT.READ_ONLY | SWT.BORDER);
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button b = new Button(ret, SWT.PUSH);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				outputDir = new DirectoryDialog(parentInc.getShell(), SWT.OPEN).open();
				CoreHub.localCfg.set(PreferenceConstants.RNN_DIR, outputDir);
				text.setText(outputDir);
			}
		});
		b.setText(Messages.XMLExporter_Change);
		outputDir = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR, CorePreferenceInitializer.getDefaultDBPath());
		text.setText(outputDir);
		Label lbPostProcess = new Label(ret, SWT.NONE);
		lbPostProcess.setText("Nachbearbeitung (%s for Quelldatei, %t für Zieldatei einsetzen)");
		lbPostProcess.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		txPostProcess = new Text(ret, SWT.BORDER);
		txPostProcess.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		txPostProcess.setText(CoreHub.localCfg.get(PreferenceConstants.POSTPROCESS, ""));
		return ret;
	}

	@Override
	public void saveComposite() {
		postProcessCommand = txPostProcess.getText();
		CoreHub.localCfg.set(PreferenceConstants.POSTPROCESS, postProcessCommand);
	}

	@Override
	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn, Properties props) {
		Result<Rechnung> res = new Result<Rechnung>();
		// QR_Generator qr = new QR_Generator();
		QR_Encoder qr = new QR_Encoder();
		String template = PlatformHelper.getBasePath("ch.elexis.ungrad.qrbills") + File.separator + "rsc"
				+ File.separator + "qrbill_template_v2.html";
		File templateFile = new File(template);
		try {
			String rawHTML = FileTool.readTextFile(templateFile);

			for (Rechnung rn : rnn) {
				try {
					BillDetails bill = new BillDetails(rn);
					replacer.put("Adressat", bill.adressat);
					replacer.put("Mandant", bill.biller);
					replacer.put("Patient", bill.patient);
					replacer.put("Rechnung", rn);
					Resolver resolver = new Resolver(replacer, true);

					String cookedHTML = resolver.resolve(rawHTML);
					String svg = qr.generate(rn, bill);
					String finished = cookedHTML.replace("[QRCODE]", svg).replace("[CURRENCY]", bill.currency)
							.replace("[AMOUNT]", bill.amount.getAmountAsString()).replace("[IBAN]", bill.IBAN)
							.replace("[BILLER]", bill.biller_address).replace("[ESRLINE]", bill.qrIBAN)
							.replace("[INFO]", Integer.toString(bill.numCons) + " Konsultationen")
							.replace("[ADDRESSEE]", bill.addressee).replace("[DUE]", bill.dateDue);

					File file = new File(outputDir, rn.getRnId() + ".html");
					// FileTool.writeTextFile(file, finished);
					FileOutputStream fout = new FileOutputStream(new File(outputDir, rn.getId() + ".pdf"));

					PdfRendererBuilder builder = new PdfRendererBuilder();
					builder.useFastMode();
					builder.withFile(file);
					builder.toStream(fout);
					builder.run();
					doPostProcess(rn.getRnId());
					res.add(new Result<Rechnung>(rn));
				} catch (Exception ex) {
					ExHandler.handle(ex);
					res.add(new Result<Rechnung>(SEVERITY.ERROR, 2, ex.getMessage(), rn, true));
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(new Result<Rechnung>(SEVERITY.ERROR, 1, "Could  not find templateFile " + template, null, true));
		}
		if (res.isOK()) {
			SWTHelper.showInfo("Ausgabe beendet", rnn.size() + " QR-Rechnung(en) wurde(n) ausgegeben");
		} else {
			SWTHelper.showError("QR-Output", "Fehler bei der Rechnungsausgabe", res.toString());

		}
		return res;
	}

	private void doPostProcess(String id) throws IOException {
		if (!postProcessCommand.isEmpty()) {
			String cmd = postProcessCommand.replace("%s", outputDir + File.separator + id + ".html").replace("%t",
					outputDir + File.separator + id);
			String[] params = cmd.split(" +");
			Runtime.getRuntime().exec(params);
		}
	}

}

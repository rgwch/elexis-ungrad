package ch.elexis.ungrad.privatrechnung_qr.rechnung;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.privatrechnung_qr.data.PreferenceConstants;
import ch.rgw.tools.StringTool;

/*******************************************************************************
 * Copyright (c) 2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
public class QR_SettingsControl extends Composite {
	String outputDirPDF;
	Combo cbPrinters;
	PrintService[] printers;
	Button cbDoPrint, cbDirectPrint, cbDoDelete, cbDebug;
	Text tOutdirPDF;
	private Button cbFaceDown;

	public QR_SettingsControl(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2, false));



		// PDF output directory
		Label l = new Label(this, SWT.NONE);
		l.setText("Zielverzeichnis für PDFs");
		l.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		tOutdirPDF = new Text(this, SWT.READ_ONLY | SWT.BORDER);
		tOutdirPDF.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button b = new Button(this, SWT.PUSH);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				outputDirPDF = new DirectoryDialog(parent.getShell(), SWT.OPEN).open();
				CoreHub.localCfg.set(PreferenceConstants.RNN_DIR_PDF, outputDirPDF);
				tOutdirPDF.setText(outputDirPDF);
			}
		});
		b.setText("Ändern");
		outputDirPDF = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR_PDF,
				CoreHub.getTempDir().getAbsolutePath());
		tOutdirPDF.setText(outputDirPDF);

		Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbDoPrint = new Button(this, SWT.CHECK);
		cbDoPrint.setText("Rechnung ausdrucken");
		cbDoPrint.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbDoPrint.setSelection(CoreHub.localCfg.get(PreferenceConstants.DO_PRINT, false));
		cbFaceDown = new Button(this, SWT.CHECK);
		cbFaceDown.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbFaceDown.setText("Druckseite unten");
		cbFaceDown.setSelection(CoreHub.localCfg.get(PreferenceConstants.FACE_DOWN, false));
		cbDirectPrint = new Button(this, SWT.CHECK);
		cbDirectPrint.setText("Direkt ausdrucken auf:");
		cbDirectPrint.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbDirectPrint.setSelection(CoreHub.localCfg.get(PreferenceConstants.DIRECT_PRINT, false));
		cbPrinters = new Combo(this, SWT.READ_ONLY);
		cbPrinters.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		printers = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService ps : printers) {
			cbPrinters.add(ps.getName());
		}
		String currentPrinter = CoreHub.localCfg.get(PreferenceConstants.DEFAULT_PRINTER, "");
		if (!StringTool.isNothing(currentPrinter)) {
			cbPrinters.setText(currentPrinter);
		}
		cbDoDelete = new Button(this, SWT.CHECK);
		cbDoDelete.setText("PDF nach dem Drucken löschen");
		cbDoDelete.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbDoDelete.setSelection(CoreHub.localCfg.get(PreferenceConstants.DELETE_AFTER_PRINT, true));
		Label sep2 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep2.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));

		cbDebug = new Button(this, SWT.CHECK);
		cbDebug.setText("Debug: HTML Zwischendateien nicht löschen");
		cbDebug.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbDebug.setSelection(CoreHub.localCfg.get(PreferenceConstants.DEBUGFILES, true));
	}

	public void doSave() {

		CoreHub.localCfg.set(PreferenceConstants.RNN_DIR_PDF, tOutdirPDF.getText());
		CoreHub.localCfg.set(PreferenceConstants.DO_PRINT, cbDoPrint.getSelection());
		CoreHub.localCfg.set(PreferenceConstants.DIRECT_PRINT, cbDirectPrint.getSelection());
		CoreHub.localCfg.set(PreferenceConstants.DELETE_AFTER_PRINT, cbDoDelete.getSelection());
		CoreHub.localCfg.set(PreferenceConstants.DEFAULT_PRINTER, cbPrinters.getText());
		CoreHub.localCfg.set(PreferenceConstants.DEBUGFILES, cbDebug.getSelection());
		CoreHub.localCfg.set(PreferenceConstants.FACE_DOWN, cbFaceDown.getSelection());
		
		if (cbPrinters.getSelectionIndex() > -1) {
			PrintService printService = printers[cbPrinters.getSelectionIndex()];
			Class[] attributes = printService.getSupportedAttributeCategories();
			// printService.getSupportedAttributeValues(arg0, arg1, arg2)
			// Attribute[] aatr=attributes.toArray();
			System.out.print(attributes.length);
		}
	}
}

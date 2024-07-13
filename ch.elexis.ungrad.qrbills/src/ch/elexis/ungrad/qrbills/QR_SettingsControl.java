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
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.pdfBills.OutputterUtil;
import ch.elexis.pdfBills.QrRnOutputter;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.rgw.tools.StringTool;

public class QR_SettingsControl extends Composite {
	String outputDirPDF;
	String outputDirXML;
	Combo cbPrinters;
	PrintService[] printers;
	Button cbQRPage, cbTarmedForm, cbDoPrint, cbDirectPrint, cbDoDelete, cbDebug, cbMissingData;
	Text tOutdirPDF;
	Text tOutdirXML;
	IConfigService cfg = ConfigServiceHolder.get();
	private Button cbFaceDown;

	public QR_SettingsControl(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2, false));

		cbMissingData = new Button(this, SWT.CHECK);
		cbMissingData.setText("Bei fehlenden administrativen Daten trotzdem ausgeben");
		cbMissingData.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbMissingData.setSelection(cfg.getLocal(PreferenceConstants.MISSING_DATA, true));

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
				cfg.setLocal(PreferenceConstants.RNN_DIR_PDF, outputDirPDF);
				cfg.setLocal(OutputterUtil.CFG_PRINT_GLOBALOUTPUTDIRS, false);
				tOutdirPDF.setText(outputDirPDF);
			}
		});
		b.setText("Ändern");

		Label lx = new Label(this, SWT.NONE);
		lx.setText("Zielverzeichnis für XMLs");
		lx.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		tOutdirXML = new Text(this, SWT.READ_ONLY | SWT.BORDER);
		tOutdirXML.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bx = new Button(this, SWT.PUSH);
		bx.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				outputDirXML = new DirectoryDialog(parent.getShell(), SWT.OPEN).open();
				cfg.setLocal(PreferenceConstants.RNN_DIR_XML, outputDirXML);
				cfg.setLocal(OutputterUtil.CFG_PRINT_GLOBALOUTPUTDIRS, false);
				tOutdirXML.setText(outputDirXML);
			}
		});

		if (OutputterUtil.useGlobalOutputDirs()) {
			outputDirPDF = OutputterUtil.getPdfOutputDir(QrRnOutputter.CFG_ROOT);
			outputDirXML = OutputterUtil.getXmlOutputDir(QrRnOutputter.CFG_ROOT);
			if(outputDirPDF.startsWith("file:/")) {
				outputDirPDF=outputDirPDF.substring("file:/".length()-1);
			}
			if(outputDirXML.startsWith("file:/")) {
				outputDirXML=outputDirXML.substring("file:/".length()-1);
			}
		} else {
			outputDirPDF = cfg.getLocal(PreferenceConstants.RNN_DIR_PDF, CoreHub.getTempDir().getAbsolutePath());
			outputDirXML = cfg.getLocal(PreferenceConstants.RNN_DIR_XML, CoreHub.getTempDir().getAbsolutePath());
		}
		tOutdirPDF.setText(outputDirPDF);
		bx.setText("Ändern");
		tOutdirXML.setText(outputDirXML);
		Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbQRPage = new Button(this, SWT.CHECK);
		cbQRPage.setText("Seite mit QR ausgeben");
		cbQRPage.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbQRPage.setSelection(cfg.getLocal(PreferenceConstants.PRINT_QR, true));
		cbTarmedForm = new Button(this, SWT.CHECK);
		cbTarmedForm.setText("Rechnungsformular ausgeben");
		cbTarmedForm.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbTarmedForm.setSelection(cfg.getLocal(PreferenceConstants.PRINT_TARMED, true));
		cbDoPrint = new Button(this, SWT.CHECK);
		cbDoPrint.setText("Rechnung ausdrucken");
		cbDoPrint.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbDoPrint.setSelection(cfg.getLocal(PreferenceConstants.DO_PRINT, false));
		cbFaceDown = new Button(this, SWT.CHECK);
		cbFaceDown.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbFaceDown.setText("Druckseite unten");
		cbFaceDown.setSelection(cfg.getLocal(PreferenceConstants.FACE_DOWN, false));
		cbDirectPrint = new Button(this, SWT.CHECK);
		cbDirectPrint.setText("Direkt ausdrucken auf:");
		cbDirectPrint.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbDirectPrint.setSelection(cfg.getLocal(PreferenceConstants.DIRECT_PRINT, false));
		cbPrinters = new Combo(this, SWT.READ_ONLY);
		cbPrinters.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		printers = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService ps : printers) {
			cbPrinters.add(ps.getName());
		}
		String currentPrinter = cfg.getLocal(PreferenceConstants.DEFAULT_PRINTER, "");
		if (!StringTool.isNothing(currentPrinter)) {
			cbPrinters.setText(currentPrinter);
		}
		cbDoDelete = new Button(this, SWT.CHECK);
		cbDoDelete.setText("PDF nach dem Drucken löschen");
		cbDoDelete.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbDoDelete.setSelection(cfg.getLocal(PreferenceConstants.DELETE_AFTER_PRINT, true));
		Label sep2 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep2.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));

		cbDebug = new Button(this, SWT.CHECK);
		cbDebug.setText("Debug: HTML Zwischendateien nicht löschen");
		cbDebug.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbDebug.setSelection(cfg.getLocal(PreferenceConstants.DEBUGFILES, true));
	}

	public void doSave() {

		cfg.setLocal(PreferenceConstants.RNN_DIR_PDF, tOutdirPDF.getText());
		cfg.setLocal(PreferenceConstants.RNN_DIR_XML, tOutdirXML.getText());
		cfg.setLocal(PreferenceConstants.PRINT_QR, cbQRPage.getSelection());
		cfg.setLocal(PreferenceConstants.PRINT_TARMED, cbTarmedForm.getSelection());
		cfg.setLocal(PreferenceConstants.DO_PRINT, cbDoPrint.getSelection());
		cfg.setLocal(PreferenceConstants.DIRECT_PRINT, cbDirectPrint.getSelection());
		cfg.setLocal(PreferenceConstants.DELETE_AFTER_PRINT, cbDoDelete.getSelection());
		cfg.setLocal(PreferenceConstants.DEFAULT_PRINTER, cbPrinters.getText());
		cfg.setLocal(PreferenceConstants.DEBUGFILES, cbDebug.getSelection());
		cfg.setLocal(PreferenceConstants.FACE_DOWN, cbFaceDown.getSelection());
		cfg.setLocal(PreferenceConstants.MISSING_DATA, cbMissingData.getSelection());

		if (cbPrinters.getSelectionIndex() > -1) {
			PrintService printService = printers[cbPrinters.getSelectionIndex()];
			Class[] attributes = printService.getSupportedAttributeCategories();
			// printService.getSupportedAttributeValues(arg0, arg1, arg2)
			// Attribute[] aatr=attributes.toArray();
			System.out.print(attributes.length);
		}
	}
}

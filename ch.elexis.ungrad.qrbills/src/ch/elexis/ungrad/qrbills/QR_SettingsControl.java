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
import ch.elexis.core.data.preferences.CorePreferenceInitializer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.qrbills.preferences.PreferenceConstants;
import ch.rgw.tools.StringTool;

public class QR_SettingsControl extends Composite {
	String outputDirPDF;
	String outputDirXML;
	Combo cbPrinters;
	PrintService[] printers;
	Button cbQRPage, cbTarmedForm, cbDoPrint, cbDirectPrint, cbDoDelete, cbDebug;
	Text tOutdirPDF;
	Text tOutdirXML;

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
		outputDirPDF = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR_PDF, CorePreferenceInitializer.getDefaultDBPath());
		tOutdirPDF.setText(outputDirPDF);
		
		Label lx=new Label(this,SWT.NONE);
		lx.setText("Zielverzeichnis für XMLs");
		lx.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		tOutdirXML=new Text(this, SWT.READ_ONLY|SWT.BORDER);
		tOutdirXML.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bx= new Button(this,SWT.PUSH);
		bx.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				outputDirXML = new DirectoryDialog(parent.getShell(), SWT.OPEN).open();
				CoreHub.localCfg.set(PreferenceConstants.RNN_DIR_XML, outputDirXML);
				tOutdirXML.setText(outputDirXML);
			}
		});
		bx.setText("Ändern");
		outputDirXML = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR_XML, CorePreferenceInitializer.getDefaultDBPath());
		tOutdirXML.setText(outputDirXML);
	
		Label sep=new Label(this, SWT.SEPARATOR|SWT.HORIZONTAL);
		sep.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbQRPage = new Button(this, SWT.CHECK);
		cbQRPage.setText("Seite mit QR ausgeben");
		cbQRPage.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbQRPage.setSelection(CoreHub.localCfg.get(PreferenceConstants.PRINT_QR, true));
		cbTarmedForm = new Button(this, SWT.CHECK);
		cbTarmedForm.setText("Rechnungsformular ausgeben");
		cbTarmedForm.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbTarmedForm.setSelection(CoreHub.localCfg.get(PreferenceConstants.PRINT_TARMED, true));
		cbDoPrint = new Button(this, SWT.CHECK);
		cbDoPrint.setText("Rechnung ausdrucken");
		cbDoPrint.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbDoPrint.setSelection(CoreHub.localCfg.get(PreferenceConstants.DO_PRINT, false));
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
		Label sep2=new Label(this, SWT.SEPARATOR|SWT.HORIZONTAL);
		sep2.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		cbDebug=new Button(this,SWT.CHECK);
		cbDebug.setText("Debug: HTML Zwischendateien nicht löschen");
		cbDebug.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cbDebug.setSelection(CoreHub.localCfg.get(PreferenceConstants.DEBUGFILES, true));
	}

	public void doSave() {

		CoreHub.localCfg.set(PreferenceConstants.RNN_DIR_PDF, tOutdirPDF.getText());
		CoreHub.localCfg.set(PreferenceConstants.RNN_DIR_XML, tOutdirXML.getText());
		CoreHub.localCfg.set(PreferenceConstants.PRINT_QR, cbQRPage.getSelection());
		CoreHub.localCfg.set(PreferenceConstants.PRINT_TARMED, cbTarmedForm.getSelection());
		CoreHub.localCfg.set(PreferenceConstants.DO_PRINT, cbDoPrint.getSelection());
		CoreHub.localCfg.set(PreferenceConstants.DIRECT_PRINT, cbDirectPrint.getSelection());
		CoreHub.localCfg.set(PreferenceConstants.DELETE_AFTER_PRINT, cbDoDelete.getSelection());
		CoreHub.localCfg.set(PreferenceConstants.DEFAULT_PRINTER, cbPrinters.getText());
		CoreHub.localCfg.set(PreferenceConstants.DEBUGFILES, cbDebug.getText());
		
		if (cbPrinters.getSelectionIndex() > -1) {
			PrintService printService = printers[cbPrinters.getSelectionIndex()];
			Class[] attributes = printService.getSupportedAttributeCategories();
			// printService.getSupportedAttributeValues(arg0, arg1, arg2)
			// Attribute[] aatr=attributes.toArray();
			System.out.print(attributes.length);
		}
	}
}

package ch.elexis.pdfBills;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import ch.elexis.core.ui.util.SWTHelper;

import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.data.Fall;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;

public class RnOutputter implements IRnOutputter {
	private static final String PDFDIR = "pdfdir";
	public static final String PLUGIN_ID = "ch.elexis.pdfBills";
	private static final String XMLDIR = "xmldir";
	private static final String CFG_ROOT = "pdf-output/";
	private static final String CFG_MARGINLEFT = "margin.left";
	private static final String CFG_MARGINRIGHT = "margin.right";
	private static final String CFG_MARGINTOP = "margin.top";
	private static final String CFG_MARGINBOTTOM = "margin.bottom";
	private static final String CFG_BESR_MARGIN_VERTICAL = "margin.besr.vertical";
	private static final String CFG_BESR_MARGIN_HORIZONTAL = "margin.besr.horizontal";
	private Text tXml;
	private Text tPdf;
	
	private Text pdfLeftMargin;
	private Text pdfRightMargin;
	private Text pdfTopMargin;
	private Text pdfBottumMagin;
	private Text pdfBesrMarginVertical;
	private Text pdfBesrMarginHorizontal;

	@Override
	public String getDescription(){
		return "PDF Output";
	}
	
	
	@Override
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn,
		Properties props){
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		final Result<Rechnung> res = new Result<Rechnung>();
		final File rsc = new File(PlatformHelper.getBasePath(PLUGIN_ID), "rsc");
		final File cfgOut = new File(CoreHub.getWritableUserDir(), "fopconfig.xml");
		final Pattern cfgPattern = Pattern.compile("(.*?)(!PATH!)(.*)$");
		
		try {
			File cfg = new File(rsc, "userconfig.xml");
			if (cfg.exists()) {
				String path = rsc.getAbsolutePath().replace('\\', '/') + "/";
				byte[] buffer = new byte[(int) cfg.length()];
				FileInputStream fis = new FileInputStream(cfg);
				fis.read(buffer);
				fis.close();
				String cfgString = new String(buffer);
				cfgString = cfgString.replaceAll("!PATH!", path);
				FileOutputStream fout = new FileOutputStream(cfgOut);
				fout.write(cfgString.getBytes());
				fout.close();
			}
		} catch (IOException ioex) {

		}
		
		try {
			progressService.runInUI(PlatformUI.getWorkbench().getProgressService(),
				new IRunnableWithProgress() {
					public void run(final IProgressMonitor monitor){
						monitor.beginTask("Exportiere Rechnungen...", rnn.size() * 10);
						int errors = 0;
						for (Rechnung rn : rnn) {
							XMLExporter ex = new XMLExporter();
							Document dRn = ex.doExport(rn, null, type, true);
							monitor.worked(1);
							if (rn.getStatus() == RnStatus.FEHLERHAFT) {
								errors++;
								continue;
							}
							String fname =
								CoreHub.localCfg.get(CFG_ROOT + XMLDIR, "") + File.separator
									+ rn.getNr() + ".xml";
							try {
								FileOutputStream fout = new FileOutputStream(fname);
								OutputStreamWriter cout = new OutputStreamWriter(fout, "UTF-8");
								XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
								xout.output(dRn, cout);
								cout.close();
								fout.close();
								// default values
								String _bottom = "1.5cm";
								String _left = "0.8cm";
								String _right = "0.7cm";
								String _top = "1.1cm";
								String _besrV = "0.75cm";
								String _besrH = "0.0cm";
								if (checkValue(pdfBottumMagin.getText().toString())
									&& checkValue(pdfLeftMargin.getText().toString())
									&& checkValue(pdfRightMargin.getText().toString())
									&& checkValue(pdfTopMargin.getText().toString())
									&& checkValue(pdfBesrMarginVertical.getText().toString())
									&& checkValue(pdfBesrMarginHorizontal.getText().toString())) {
									_bottom = pdfBottumMagin.getText().toString() + "cm";
									_left = pdfLeftMargin.getText().toString() + "cm";
									_right = pdfRightMargin.getText().toString() + "cm";
									_top = pdfTopMargin.getText().toString() + "cm";
									_besrV = pdfBesrMarginVertical.getText().toString() + "cm";
									_besrH = pdfBesrMarginHorizontal.getText().toString() + "cm";
								}
								ElexisPDFGenerator epdf =
									new ElexisPDFGenerator(cfgOut.getAbsolutePath());
								epdf.setMarginData(_left, _right, _top, _bottom, _besrV, _besrH);
								File xsl = new File(rsc, "section2.xsl");
								File pdf =
									new File(CoreHub.localCfg.get(CFG_ROOT + PDFDIR, "")
										+ File.separator + rn.getNr() + "_esr.pdf");
								epdf.generateMainPDF(new File(fname), xsl, pdf, "P1");
								xsl = new File(rsc, "1359.xsl");
								pdf =
									new File(CoreHub.localCfg.get(CFG_ROOT + PDFDIR, "")
										+ File.separator + rn.getNr() + "_rf.pdf");
								epdf.generateMainPDF(new File(fname), xsl, pdf, "P1");
								int status_vorher = rn.getStatus();
								if ((status_vorher == RnStatus.OFFEN)
									|| (status_vorher == RnStatus.MAHNUNG_1)
									|| (status_vorher == RnStatus.MAHNUNG_2)
									|| (status_vorher == RnStatus.MAHNUNG_3)) {
									rn.setStatus(status_vorher + 1);
								}
							} catch (Exception e1) {
								ExHandler.handle(e1);
								SWTHelper.showError("Fehler bei Trustx", "Konnte Datei " + fname
									+ " nicht schreiben");
								rn.reject(RnStatus.REJECTCODE.INTERNAL_ERROR, "write error: "
									+ fname);
								continue;
							}
							monitor.worked(1);
						}
						monitor.done();
						if (errors > 0) {
							SWTHelper
								.alert(
									"Fehler bei der Übermittlung",
									Integer.toString(errors)
										+ " Rechnungen waren fehlerhaft. Sie können diese unter Rechnungen mit dem Status fehlerhaft aufsuchen und korrigieren");
						} else {
							SWTHelper.showInfo("Übermittlung beendet",
								"Es sind keine Fehler aufgetreten");
						}
					}
				}, null);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(Result.SEVERITY.ERROR, 2, ex.getMessage(), null, true);
			ErrorDialog.openError(null, "Fehler bei der Ausgabe",
				"Konnte TrustX-Transmit nicht starten", ResultAdapter.getResultAsStatus(res));
			return res;
		}
		return res;
	}
	
	@Override
	public boolean canStorno(Rechnung rn){
		return false;
	}
	
	@Override
	public boolean canBill(Fall fall){
		return true;
	}
	
	@Override
	public Control createSettingsControl(final Object parent){
		final Composite cParent=(Composite)parent;
		Composite ret = new Composite(cParent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData());
		ret.setLayout(new GridLayout(2, false));
		Button bXML = new Button(ret, SWT.PUSH);
		bXML.setText("XML Verzeichnis");
		tXml = new Text(ret, SWT.BORDER | SWT.READ_ONLY);
		tXml.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bPDF = new Button(ret, SWT.PUSH);
		bPDF.setText("PDF Verzeichnis");
		tPdf = new Text(ret, SWT.BORDER | SWT.READ_ONLY);
		tPdf.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		new Label(ret, 256).setText("Rand links [cm]");
		pdfLeftMargin = new Text(ret, 128);
		pdfLeftMargin.setLayoutData(SWTHelper.getFillGridData(2, true, 2, false));
		pdfLeftMargin.setText(CoreHub.localCfg.get(CFG_ROOT + CFG_MARGINLEFT, "1.5"));
		new Label(ret, 512).setText("Rand rechts [cm]");
		pdfRightMargin = new Text(ret, 128);
		pdfRightMargin.setLayoutData(SWTHelper.getFillGridData(2, true, 2, false));
		pdfRightMargin.setText(CoreHub.localCfg.get(CFG_ROOT + CFG_MARGINRIGHT, "0.7"));
		new Label(ret, 1024).setText("Rand oben [cm]");
		pdfTopMargin = new Text(ret, 128);
		pdfTopMargin.setLayoutData(SWTHelper.getFillGridData(2, true, 2, false));
		pdfTopMargin.setText(CoreHub.localCfg.get(CFG_ROOT + CFG_MARGINTOP, "1"));
		new Label(ret, 128).setText("Rand unten [cm]");
		pdfBottumMagin = new Text(ret, 128);
		pdfBottumMagin.setLayoutData(SWTHelper.getFillGridData(2, true, 2, false));
		pdfBottumMagin.setText(CoreHub.localCfg.get(CFG_ROOT + CFG_MARGINBOTTOM, "1.5"));
		new Label(ret, 128).setText("BESR Abstand zu Rand unten [cm]");
		pdfBesrMarginVertical = new Text(ret, 128);
		pdfBesrMarginVertical.setLayoutData(SWTHelper.getFillGridData(2, true, 2, false));
		pdfBesrMarginVertical
			.setText(CoreHub.localCfg.get(CFG_ROOT + CFG_BESR_MARGIN_VERTICAL, "0.75"));
		new Label(ret, 128).setText("BESR Abstand zu Rand rechts [cm]");
		pdfBesrMarginHorizontal = new Text(ret, 128);
		pdfBesrMarginHorizontal.setLayoutData(SWTHelper.getFillGridData(2, true, 2, false));
		pdfBesrMarginHorizontal.setText(CoreHub.localCfg
			.get(CFG_ROOT + CFG_BESR_MARGIN_HORIZONTAL, "0.0"));
		
		bXML.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				DirectoryDialog dd = new DirectoryDialog(cParent.getShell());
				String dir = dd.open();
				if (dir != null) {
					tXml.setText(dir);
				}
			}
			
		});
		bPDF.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				DirectoryDialog dd = new DirectoryDialog(cParent.getShell());
				String dir = dd.open();
				if (dir != null) {
					tPdf.setText(dir);
				}
			}
			
		});
		tXml.setText(CoreHub.localCfg.get(CFG_ROOT + XMLDIR, ""));
		tPdf.setText(CoreHub.localCfg.get(CFG_ROOT + PDFDIR, ""));
		return (Control) ret;
	}
	
	@Override
	public void saveComposite(){
		if (checkValue(pdfBottumMagin.getText().toString())
			&& checkValue(pdfLeftMargin.getText().toString())
			&& checkValue(pdfRightMargin.getText().toString())
			&& checkValue(pdfTopMargin.getText().toString())
			&& checkValue(pdfBesrMarginVertical.getText().toString())
			&& checkValue(pdfBesrMarginHorizontal.getText().toString())) {
			
			CoreHub.localCfg.set(CFG_ROOT + CFG_MARGINLEFT, pdfLeftMargin.getText().toString());
			CoreHub.localCfg.set(CFG_ROOT + CFG_MARGINRIGHT, pdfRightMargin.getText().toString());
			CoreHub.localCfg.set(CFG_ROOT + CFG_MARGINTOP, pdfTopMargin.getText().toString());
			CoreHub.localCfg.set(CFG_ROOT + CFG_MARGINBOTTOM, pdfBottumMagin.getText().toString());
			CoreHub.localCfg.set(CFG_ROOT + CFG_BESR_MARGIN_VERTICAL, pdfBesrMarginVertical.getText()
				.toString());
			CoreHub.localCfg.set(CFG_ROOT + CFG_BESR_MARGIN_HORIZONTAL, pdfBesrMarginHorizontal
				.getText().toString());
		}
		
		CoreHub.localCfg.set(CFG_ROOT + XMLDIR, tXml.getText());
		CoreHub.localCfg.set(CFG_ROOT + PDFDIR, tPdf.getText());
		CoreHub.localCfg.flush();
	}
	
	public boolean checkValue(String input){
		try {
			
			Float.parseFloat(input);
			return true;
		} catch (NumberFormatException e) {
			// s is not numeric
			
			return false;
		}
	}

}

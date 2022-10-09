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
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.*;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.privatrechnung_qr.data.PreferenceConstants;
import ch.elexis.ungrad.qrbills.PDF_Printer;
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
	
	public String getDescription(){
		return "Privatrechnung QR PDF";
	}
	
	/**
	 * We'll take all sorts of bills
	 */
	public boolean canBill(final Fall fall){
		return true;
	}
	
	/**
	 * We never storno
	 */
	public boolean canStorno(final Rechnung rn){
		return false;
	}
	
	/**
	 * Create the Control that will be presented to the user before selecting the bill output
	 * target. Here we simply chose a template to use for the bill. In fact we need two templates: a
	 * template for the page with summary and giro and a template for the other pages
	 */
	public Object createSettingsControl(Object parent){
		qrs = new QR_SettingsControl((Composite) parent);
		return qrs;
	}
	
	public void saveComposite(){
		qrs.doSave();
	}
	
	/**
	 * Print the bill(s)
	 */
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn,
		final Properties props){
		final Result<Rechnung> result = new Result<Rechnung>();
		
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		final Result<Rechnung> res = new Result<Rechnung>();
		try {
			progressService.runInUI(PlatformUI.getWorkbench().getProgressService(),
				new IRunnableWithProgress() {
					
					@Override
					public void run(final IProgressMonitor monitor){
						monitor.beginTask("Drucke Rechnungen", rnn.size() * 3);
						for (Rechnung rn : rnn) {
							try {
								res.add(doPrint(rn, monitor, type));
								int status_vorher = rn.getStatus();
								if ((status_vorher == RnStatus.OFFEN)
									|| (status_vorher == RnStatus.MAHNUNG_1)
									|| (status_vorher == RnStatus.MAHNUNG_2)
									|| (status_vorher == RnStatus.MAHNUNG_3)) {
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
								res.add(SEVERITY.WARNING, 5, "Output error", rn, true);
							}
						}
						monitor.done();
					}
				}, null);
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(Result.SEVERITY.ERROR, 2, ex.getMessage(), null, true);
			ErrorDialog.openError(null, "Exception", "Exception",
				ResultAdapter.getResultAsStatus(res));
			return res;
		}
		if (!result.isOK()) {
			ResultAdapter.displayResult(result, "Fehler beim Rechnungsdruck");
		}
		return result;
	}
	
	private Result<Rechnung> doPrint(final Rechnung rn, final IProgressMonitor monitor, TYPE type)
		throws Exception{
		String default_template = PlatformHelper.getBasePath("ch.elexis.ungrad.qrbills")
			+ File.separator + "rsc" + File.separator + "qrbill_template_p1.html";
		String fname = "";
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
		
		Result<Rechnung> ret = new Result<Rechnung>();
		Fall fall = rn.getFall();
		Kontakt biller = rn.getMandant();
		ElexisEventDispatcher.fireSelectionEvent(fall);
		Kontakt adressat = fall.getGarant();
		if (!adressat.isValid()) {
			adressat = fall.getPatient();
		}
		replacer.put("Adressat", adressat);
		replacer.put("Mandant", biller);
		replacer.put("Rechnung", rn);
		Resolver resolver = new Resolver(replacer, true);
		String cookedHtml=resolver.resolve(rawHTML);
		List<Konsultation> kons = rn.getKonsultationen();
		Collections.sort(kons, new Comparator<Konsultation>() {
			TimeTool t0 = new TimeTool();
			TimeTool t1 = new TimeTool();
			
			public int compare(final Konsultation arg0, final Konsultation arg1){
				t0.set(arg0.getDatum());
				t1.set(arg1.getDatum());
				return t0.compareTo(t1);
			}
			
		});
		// Leistungen und Artikel gruppieren
		Money sum = new Money();
		HashMap<String, List<Verrechnet>> groups = new HashMap<String, List<Verrechnet>>();
		for (Konsultation k : kons) {
			List<Verrechnet> vv = k.getLeistungen();
			for (Verrechnet v : vv) {
				Money netto = v.getNettoPreis();
				netto.multiply(v.getZahl());
				sum.addMoney(netto);
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
		
		return ret;
	}
}

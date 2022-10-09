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

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.data.Fall;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.ungrad.qrbills.PDF_Printer;
import ch.elexis.ungrad.qrbills.QR_Encoder;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;

public class RechnungsDrucker implements IRnOutputter {
	private QR_SettingsControl qrs;
	private QR_Encoder qr = new QR_Encoder();
	private PDF_Printer printer = new PDF_Printer();
	
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
							doPrint(rn, monitor, type, res);
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
	
	private void doPrint(final Rechnung rn, final IProgressMonitor monitor, TYPE type, Result<Rechnung> res){
		
	}
}

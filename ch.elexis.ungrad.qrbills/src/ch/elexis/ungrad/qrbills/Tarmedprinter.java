package ch.elexis.ungrad.qrbills;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.interfaces.IRnOutputter.TYPE;
import ch.elexis.data.Rechnung;
import ch.elexis.views.RnPrintView2;

public class Tarmedprinter {
	RnPrintView2 rnp;
	IWorkbenchPage rnPage;

	public Tarmedprinter() {
		rnPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public boolean print(Rechnung rn) throws PartInitException {
		rnp = (RnPrintView2) rnPage.showView(RnPrintView2.ID);
		rnp.doPrint(rn, TYPE.ORIG, null, false, true, false, null);
		return false;
	}

}

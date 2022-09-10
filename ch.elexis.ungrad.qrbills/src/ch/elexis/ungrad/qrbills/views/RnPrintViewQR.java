package ch.elexis.ungrad.qrbills.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jdom.Document;

import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.tarmed.printer.XML40Printer;
import ch.elexis.tarmed.printer.XML44Printer;

public class RnPrintViewQR extends ViewPart {
	public static final String ID = "ch.elexis.ungrad.qrbills.printviewqr";
	TextContainer text;

	@Override
	public void createPartControl(final Composite parent) {
		text = new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent, new ITextPlugin.ICallback() {

			@Override
			public void save() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean saveAs() {
				// TODO Auto-generated method stub
				return false;
			}
		});
		text.getPlugin().setParameter(ITextPlugin.Parameter.NOUI);

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public boolean doPrint(final Rechnung rn, final IRnOutputter.TYPE rnType, final String saveFile,
			final boolean withESR, final boolean withForms, final boolean doVerify, final IProgressMonitor monitor) {
		XMLExporter xmlex = new XMLExporter();
		Document xmlRn = xmlex.doExport(rn, saveFile, rnType, doVerify);
		if (rn.getStatus() == RnStatus.FEHLERHAFT) {
			return false;
		}
		// check if we have all req. text templates
		// initializeRequiredTemplates();

		XML44Printer xmlPrinter = new XML44Printer(text);
		return xmlPrinter.doPrint(rn, xmlRn, rnType, saveFile, withESR, withForms, doVerify, monitor);

	}

}

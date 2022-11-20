package ch.elexis.ungrad.text.templator.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public class OdfTemplateFieldsDisplay extends Composite {
	private IAction printAction;
	
	public OdfTemplateFieldsDisplay(Composite parent) {
		super(parent,SWT.NONE);
		if (parent.getLayout() instanceof GridLayout) {
			setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		}
		FormToolkit tk = UiDesk.getToolkit();
		setLayout(new GridLayout());
		makeActions();
		ToolBarManager tbm = new ToolBarManager(SWT.HORIZONTAL);
		tbm.add(printAction);
	
	}
	
	private void makeActions() {
		printAction = new Action("Ausgeben") {
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText("Gibt dieses Dokument mit dem konfigurierten Ausgabeprogramm aus");
			}

			@Override
			public void run() {
				try {
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

	}
}

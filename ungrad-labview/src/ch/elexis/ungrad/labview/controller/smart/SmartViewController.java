package ch.elexis.ungrad.labview.controller.smart;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.data.Patient;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

public class SmartViewController implements IObserver {
	TreeViewer tvSmart;
	Controller controller;
	SmartTreeColumns stc;

	SmartContentProvider scp;

	public SmartViewController(Controller parent) {
		controller = parent;
		scp = new SmartContentProvider(controller.getLRS());
		LabResultsSheet lrs = controller.getLRS();
		lrs.addObserver(this);

	}

	@Override
	public void signal(Object message) {
		if (message instanceof Patient) {
			stc.reload(controller);
			tvSmart.setInput(message);
		}

	}

	public LabResultsSheet getLRS() {
		return controller.getLRS();
	}

	public Control createControl(Composite parent) {
		tvSmart = new TreeViewer(parent);
		tvSmart.setContentProvider(scp);
		tvSmart.setUseHashlookup(true);
		Tree tree = tvSmart.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		stc = new SmartTreeColumns(tvSmart);
		tvSmart.setInput(scp);
		return tree;
	}

}

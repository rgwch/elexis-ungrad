package ch.elexis.ungrad.labview.controller.full;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.ungrad.labview.controller.Controller;

public class FullViewController {
	TreeViewer tvFull;
	Controller controller;
	
	public FullViewController(Controller parent) {
		controller=parent;
	}
	
	public Control createControl(CTabFolder parent) {
		tvFull = new TreeViewer(parent);
		Tree tree = tvFull.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		//tvFull.setContentProvider(new FullContentProvider(lcp.lrs));
		tvFull.setLabelProvider(new FullLabelProvider());
		parent.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem it = (CTabItem) e.item;

			}

		});
		return tree;
	}
}

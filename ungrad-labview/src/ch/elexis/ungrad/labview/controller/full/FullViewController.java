package ch.elexis.ungrad.labview.controller.full;


import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.data.Patient;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.labview.controller.Controller;

public class FullViewController implements IObserver {
	TreeViewer tvFull;
	Controller controller;
	
	public FullViewController(Controller parent) {
		controller=parent;
		parent.getLRS().addObserver(this);
	}
	
	public Control createControl(Composite parent) {
		tvFull = new TreeViewer(parent);
		Tree tree = tvFull.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tvFull.setContentProvider(new FullContentProvider(controller.getLRS()));
		tvFull.setLabelProvider(new FullLabelProvider());
		return tree;
	}

	@Override
	public void signal(Object message) {
		if(message instanceof Patient){
			tvFull.setInput(message);
		}
	}
}

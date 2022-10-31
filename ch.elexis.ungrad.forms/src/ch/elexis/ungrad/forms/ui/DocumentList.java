package ch.elexis.ungrad.forms.ui;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;

import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.forms.model.Controller;
import ch.elexis.ungrad.forms.model.PreferenceConstants;
import ch.elexis.ungrad.pdf.Manager;
import ch.rgw.tools.ExHandler;

public class DocumentList extends Composite {
	private TableViewer tv;
	private Controller controller;
	
	public DocumentList(Composite parent, Controller controller) {
		super(parent, SWT.NONE);
		this.controller=controller;
		setLayoutData(SWTHelper.getFillGridData());
		setLayout(new GridLayout());
		tv = new TableViewer(this);
		tv.setContentProvider(controller);
		tv.setLabelProvider(controller);
		tv.setComparator(new ViewerComparator() {

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((String) e2).compareTo((String) e1);
			}

		});
		tv.setInput(ElexisEventDispatcher.getSelectedPatient());
		tv.getControl().setLayoutData(SWTHelper.getFillGridData());

	}

	public void addSelectionListener(ISelectionChangedListener listener) {
		tv.addSelectionChangedListener(listener);
	}
	
	public void addDoubleclickListener(IDoubleClickListener listener) {
		tv.addDoubleClickListener(listener);
	}
	
	public String getSelection() {
		IStructuredSelection sel=tv.getStructuredSelection();
		if(sel.isEmpty()) {
			return null;
		}else {
			return (String)sel.getFirstElement();
		}
	}
	public void output() {
		IStructuredSelection sel = tv.getStructuredSelection();
		if (!sel.isEmpty()) {
			String selected = (String)sel.getFirstElement();
			File dir = controller.getOutputDirFor(null);
			File outfile = new File(dir, selected + ".pdf");
			Manager m = new Manager();
			try {
				m.printFromPDF(outfile, "");
			} catch (IOException | PrinterException e) {
				ExHandler.handle(e);
				SWTHelper.showError("Fehler bei Ausgabe", e.getMessage());
			}
		}
	}

	void setPatient(Patient pat) {
		tv.setInput(pat);
	}
}

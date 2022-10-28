package ch.elexis.ungrad.forms;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;

public class DocumentList extends Composite {
	private TableViewer tv;
	
	public DocumentList(Composite parent, Controller controller) {
		super(parent,SWT.NONE);
		setLayoutData(SWTHelper.getFillGridData());
		setLayout(new GridLayout());
		tv = new TableViewer(this);
		tv.setContentProvider(controller);
		tv.setLabelProvider(controller);
		tv.setComparator(new ViewerComparator());
		tv.setInput(ElexisEventDispatcher.getSelectedPatient());
		tv.getControl().setLayoutData(SWTHelper.getFillGridData());

	}
	
	void setPatient(Patient pat){
		tv.setInput(pat);
	}
}

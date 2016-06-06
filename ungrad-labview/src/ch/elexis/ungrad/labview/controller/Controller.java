package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.views.LaborView;

public class Controller {
	LabContentProvider lcp;
	LaborView view;
	TableViewer tv;
	LabTableColumns cols;
	
	public Controller(LaborView view){
		lcp=new LabContentProvider();
		this.view=view;
	}
	
	public Control createPartControl(Composite parent){
		tv=new TableViewer(parent);
		tv.setContentProvider(lcp);
		tv.setLabelProvider(new LabLabelProvider());
		tv.setUseHashlookup(true);
		Table table=tv.getTable();
		cols=new LabTableColumns(lcp.lrs,tv, 10);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		tv.setInput(lcp);
		return table;
	}
	
	public void setPatient(Patient pat) throws ElexisException{
			lcp.setPatient(pat);
			cols.reload(lcp);
			tv.setInput(pat);
	}
	
	public void dispose(){
		cols.dispose();
	}
	
}

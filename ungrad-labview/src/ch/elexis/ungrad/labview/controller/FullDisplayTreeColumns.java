package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;

import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

public class FullDisplayTreeColumns {
	TreeViewer tv;
	LabResultsSheet lrs;
	TreeViewerColumn[] cols;
	
	public FullDisplayTreeColumns(LabResultsSheet lrs, TreeViewer tv) {
		this.tv=tv;
		this.lrs=lrs;
	}
	
	public void setPatient(Patient pat){
		tv.getTree().removeAll();
		lrs.getItems();
		lrs.getDates();
	}
}

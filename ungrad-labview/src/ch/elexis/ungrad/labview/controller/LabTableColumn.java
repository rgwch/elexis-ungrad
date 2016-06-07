package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;

public class LabTableColumn {
	TableViewerColumn tvc;
	String label="";
	CellLabelProvider clp;
	int width=0;
	public LabTableColumn(TableViewer tv){
		tvc=new TableViewerColumn(tv, SWT.CENTER);
		tvc.getColumn().setResizable(true);
	}
	public void setLabel(String name){
		label=name;
		tvc.getColumn().setText(name);
	}
	public void setWidth(int w){
		width=w;
		tvc.getColumn().setWidth(w);
	}
	public void setLabelProvider(CellLabelProvider lp){
		clp=lp;
		tvc.setLabelProvider(clp);
	}
}

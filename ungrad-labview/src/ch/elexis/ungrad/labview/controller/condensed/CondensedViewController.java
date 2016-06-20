package ch.elexis.ungrad.labview.controller.condensed;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.data.Patient;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

public class CondensedViewController implements IObserver {
	TreeViewer tvSummary;
	LabSummaryTreeColumns colsSummary;
	LabSummaryContentProvider lcp;
	Controller controller;

	public CondensedViewController(Controller parent){
		controller=parent;
		LabResultsSheet lrs=controller.getLRS();
		lrs.addObserver(this);
		lcp=new LabSummaryContentProvider(lrs);
	}
	
	public TreeViewer getViewer(){
		return tvSummary;
	}
	
	public Control createControl(Composite parent) {
		tvSummary = new TreeViewer(parent);
		tvSummary.setContentProvider(lcp);
		tvSummary.setUseHashlookup(true);
		Tree tree = tvSummary.getTree();
		colsSummary = new LabSummaryTreeColumns(this);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tvSummary.setAutoExpandLevel(2);
		tvSummary.setInput(lcp);
		//loadState();
		return tree;
	}
	
	public void dispose() {
		controller.getLRS().removeObserver(this);
		colsSummary.dispose();
	}
	
	public String getState(){
		StringBuilder cw = new StringBuilder();
		for (int i = 0; i < colsSummary.cols.length; i++) {
			cw.append(Integer.toString(colsSummary.cols[i].getColumn().getWidth())).append(",");
		}
		String widths = cw.substring(0, cw.length() - 1);
		return widths;
	}
	
	public void setState(String state){
		int max = colsSummary.cols.length;
		int i = 0;
		for (String w : state.split(",")) {
			if (i < max) {
				colsSummary.cols[i].getColumn().setWidth(Integer.parseInt(w));
			}
		}
	}
	
	public Exporter getExporter() {
		return new Exporter(lcp);
	}

	@Override
	public void signal(Object message) {
		if(message instanceof Patient){
			colsSummary.reload(lcp);
			tvSummary.setInput(message);
		}
	}
}

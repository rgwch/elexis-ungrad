package ch.elexis.ungrad.labview.controller.smart;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;

import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.controller.DateResultLabelProvider;
import ch.elexis.ungrad.labview.controller.ItemRangeLabelProvider;
import ch.elexis.ungrad.labview.controller.ItemTextLabelProvider;
import ch.rgw.tools.TimeTool;

public class SmartTreeColumns {
	int numColumns=6;
	TreeViewer tv;
	TreeViewerColumn[] cols;
	String[] headings = { "Parameter", "Referenz" };
	int[] widths = { 150, 120 };

	public SmartTreeColumns(TreeViewer tv) {
		this.tv = tv;
	}

	public void reload(Controller svc) {
		for (TreeColumn tc : tv.getTree().getColumns()) {
			tc.dispose();
		}
		TimeTool[] dates = svc.getLRS().getDates();
		int extra=0;
		int cLen=dates.length+2;
		if(dates.length>numColumns){
			extra=1;
			cLen=numColumns+2;
		}
		cols = new TreeViewerColumn[cLen];
		for (int i = 0; i < 2; i++) {
			cols[i] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i].getColumn().setText(headings[i]);
			cols[i].getColumn().setWidth(widths[i]);
		}
		cols[0].setLabelProvider(new ItemTextLabelProvider());
		cols[1].setLabelProvider(new ItemRangeLabelProvider(svc.getLRS()));

		for (int i = 2; i < cLen - extra; i++) {
			cols[i] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i].getColumn().setText(dates[i - 2].toString(TimeTool.DATE_GER));
			cols[i].getColumn().setWidth(80);
			cols[i].setLabelProvider(new DateResultLabelProvider(svc, dates[i - 2]));
		}
	
		if (extra>0) {
			cols[cLen-1] = new TreeViewerColumn(tv, SWT.NONE);
			cols[cLen-1].getColumn().setText("früher");
			cols[cLen-1].getColumn().setWidth(100);
			cols[cLen-1].setLabelProvider(new SmartSummaryLabelProvider(svc, dates[cLen - 3]));
		}
	}

}

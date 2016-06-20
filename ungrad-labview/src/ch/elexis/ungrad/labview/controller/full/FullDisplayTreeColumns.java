package ch.elexis.ungrad.labview.controller.full;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;

import ch.elexis.ungrad.labview.controller.ItemRangeLabelProvider;
import ch.elexis.ungrad.labview.controller.ItemTextLabelProvider;
import ch.rgw.tools.TimeTool;

public class FullDisplayTreeColumns {
	TreeViewer tv;
	TreeViewerColumn[] cols;
	String[] headings = {
		"Parameter", "Referenz"
	};
	int[] widths = {
		150, 120
	};
	
	public FullDisplayTreeColumns(TreeViewer tv){
		this.tv = tv;
	}
	
	public void reload(FullViewController fc){
		tv.getTree().removeAll();
		TimeTool[] dates = fc.getLRS().getDates();
		cols = new TreeViewerColumn[dates.length + 2];
		for (int i = 0; i < 2; i++) {
			cols[i] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i].getColumn().setText(headings[i]);
			cols[i].getColumn().setWidth(widths[i]);
		}
		cols[0].setLabelProvider(new ItemTextLabelProvider());
		cols[1].setLabelProvider(new ItemRangeLabelProvider(fc.getLRS()));
		for (int i = 2; i < dates.length; i++) {
			cols[i] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i].getColumn().setText(dates[i - 2].toString(TimeTool.DATE_GER));
			cols[i].getColumn().setWidth(100);
			cols[i].setLabelProvider(new FullLabelProvider(fc, dates[i - 2]));
		}
	}
}

package ch.elexis.ungrad.labview.controller.full;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;

import ch.elexis.ungrad.labview.controller.DateResultLabelProvider;
import ch.elexis.ungrad.labview.controller.ItemRangeLabelProvider;
import ch.elexis.ungrad.labview.controller.ItemTextLabelProvider;
import ch.rgw.tools.TimeTool;

public class FullDisplayTreeColumns {
	TreeViewerColumn[] cols;
	String[] headings = { "Parameter", "Referenz" };
	int[] widths = { 150, 120 };
	FullViewController ctl;

	public FullDisplayTreeColumns(FullViewController controller) {
		ctl = controller;
	}

	public void reload() {
		for (TreeColumn tc : ctl.tvFull.getTree().getColumns()) {
			tc.dispose();
		}
		TimeTool[] dates = ctl.getLRS().getDates();
		cols = new TreeViewerColumn[dates.length + 2];
		for (int i = 0; i < 2; i++) {
			cols[i] = new TreeViewerColumn(ctl.tvFull, SWT.NONE);
			cols[i].getColumn().setText(headings[i]);
			cols[i].getColumn().setWidth(widths[i]);
		}
		cols[0].setLabelProvider(new ItemTextLabelProvider());
		cols[1].setLabelProvider(new ItemRangeLabelProvider(ctl.getLRS()));
		for (int i = dates.length - 1; i > -1; i--) {
			cols[i + 2] = new TreeViewerColumn(ctl.tvFull, SWT.NONE);
			cols[i + 2].getColumn().setText(dates[i].toString(TimeTool.DATE_GER));
			cols[i + 2].getColumn().setWidth(80);
			cols[i + 2].setLabelProvider(new DateResultLabelProvider(ctl.controller, dates[i]));
			cols[i + 2].setEditingSupport(ctl.createEditingSupportFor(cols[i + 2], dates[i]));
		}
	}

}

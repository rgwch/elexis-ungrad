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
	static final int DEFAULT_WIDTH = 100;
	static final int MAX_WIDTH=300;
	int numColumns = 7;
	TreeViewer tv;
	TreeViewerColumn[] cols;
	String[] headings = {
		"Parameter", "Referenz"
	};
	int[] widths = {
		150, 120
	};
	int colWidth;
	
	public SmartTreeColumns(TreeViewer tv){
		this.tv = tv;
		colWidth = DEFAULT_WIDTH;
	}
	
	public void saveColLayout(){
		if (cols == null) {
			if (widths == null) {
				widths = new int[] {
					150, 120
				};
			}
		} else {
			if (cols.length > widths.length) {
				widths = new int[cols.length];
			}
			
			for (int i = 0; i < cols.length-1; i++) {
				widths[i] = widths[i] == 0 ? colWidth : widths[i];
				if (cols[i] != null) {
					TreeColumn tc = cols[i].getColumn();
					if (tc != null) {
						widths[i] = cols[i].getColumn().getWidth();
					}
				}
			}
		}
	}
	
	public void setColWidths(int[] widths){
		if (widths != null && widths.length > 1) {
			this.widths = widths;
		} else {
			this.widths = new int[] {
				150, 120
			};
		}
	}
	
	public void reload(Controller svc){
		for (TreeColumn tc : tv.getTree().getColumns()) {
			tc.dispose();
		}
		TimeTool[] dates = svc.getLRS().getDates();
		int extra = 0;
		int colOffset = 2;
		int cLen = dates.length;
		if (dates.length > numColumns) {
			extra = 1;
			cLen = numColumns;
		}
		cols = new TreeViewerColumn[cLen + colOffset + extra];
		for (int i = 0; i < 2; i++) {
			cols[i] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i].getColumn().setText(headings[i]);
			cols[i].getColumn().setWidth(widths[i]);
		}
		cols[0].setLabelProvider(new ItemTextLabelProvider());
		cols[1].setLabelProvider(new ItemRangeLabelProvider(svc.getLRS()));
		
		for (int i = 0; i < cLen - extra; i++) {
			cols[colOffset + i] = new TreeViewerColumn(tv, SWT.NONE);
			cols[colOffset + i].getColumn()
				.setText(dates[dates.length - 1 - i].toString(TimeTool.DATE_GER));
			int w = colWidth;
			if (widths.length > colOffset + i) {
				w = widths[colOffset + i];
			}
			cols[colOffset + i].getColumn().setWidth(w);
			cols[colOffset + i]
				.setLabelProvider(new DateResultLabelProvider(svc, dates[dates.length - 1 - i]));
		}
		
		if (extra > 0) {
			cols[cols.length - 1] = new TreeViewerColumn(tv, SWT.NONE);
			cols[cols.length - 1].getColumn().setText("früher");
			int w = colWidth;
			if (widths.length > cols.length - 1) {
				w = widths[cols.length - 1];
			}
			cols[cols.length - 1].getColumn().setWidth(w);
			cols[cols.length - 1].setLabelProvider(
				new SmartSummaryLabelProvider(svc, dates[dates.length - cLen + 1]));
		}
	}
	
}

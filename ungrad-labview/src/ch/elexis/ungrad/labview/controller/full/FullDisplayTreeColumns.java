package ch.elexis.ungrad.labview.controller.full;

/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;

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
		for(TreeColumn tc:tv.getTree().getColumns()){
			tc.dispose();
		}
		TimeTool[] dates = fc.getLRS().getDates();
		cols = new TreeViewerColumn[dates.length + 2];
		for (int i = 0; i < 2; i++) {
			cols[i] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i].getColumn().setText(headings[i]);
			cols[i].getColumn().setWidth(widths[i]);
		}
		cols[0].setLabelProvider(new ItemTextLabelProvider());
		cols[1].setLabelProvider(new ItemRangeLabelProvider(fc.getLRS()));
		for (int i = dates.length-1; i >-1; i--) {
			cols[i+2] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i+2].getColumn().setText(dates[i].toString(TimeTool.DATE_GER));
			cols[i+2].getColumn().setWidth(80);
			cols[i+2].setLabelProvider(new FullLabelProvider(fc, dates[i]));
		}
	}
}

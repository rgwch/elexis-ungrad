package ch.elexis.ungrad.labview.controller.full;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
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

import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.controller.DateResultLabelProvider;
import ch.elexis.ungrad.labview.controller.ItemRangeLabelProvider;
import ch.elexis.ungrad.labview.controller.ItemTextLabelProvider;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsSheet;
import ch.rgw.tools.TimeTool;

public class FullDisplayTreeColumns {
	TreeViewer tv;
	TreeViewerColumn[] cols;
	String[] headings = { "Parameter", "Referenz" };
	int[] widths = { 150, 120 };
	LabResultEditor lce;

	public FullDisplayTreeColumns(TreeViewer tv) {
		this.tv = tv;
		lce = new LabResultEditor(tv.getTree());
	}

	public void reload(Controller ctl) {
		for (TreeColumn tc : tv.getTree().getColumns()) {
			tc.dispose();
		}
		TimeTool[] dates = ctl.getLRS().getDates();
		cols = new TreeViewerColumn[dates.length + 2];
		for (int i = 0; i < 2; i++) {
			cols[i] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i].getColumn().setText(headings[i]);
			cols[i].getColumn().setWidth(widths[i]);
		}
		cols[0].setLabelProvider(new ItemTextLabelProvider());
		cols[1].setLabelProvider(new ItemRangeLabelProvider(ctl.getLRS()));
		for (int i = dates.length - 1; i > -1; i--) {
			cols[i + 2] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i + 2].getColumn().setText(dates[i].toString(TimeTool.DATE_GER));
			cols[i + 2].getColumn().setWidth(80);
			cols[i + 2].setLabelProvider(new DateResultLabelProvider(ctl, dates[i]));
			cols[i + 2].setEditingSupport(createEditingSupportFor(cols[i + 2], ctl.getLRS(), dates[i]));
		}
	}

	private EditingSupport createEditingSupportFor(TreeViewerColumn tvc, LabResultsSheet lrs, TimeTool colDate) {
		return new EditingSupport(tv) {

			@Override
			protected void setValue(Object element, Object value) {

			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Item) {
					//return "yeah";
					return lrs.getResultForDate((Item) element, colDate);
				} else {
					return "";
				}
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return lce;
			}

			@Override
			protected boolean canEdit(Object element) {
				return element instanceof Item;
			}
		};
	}
}

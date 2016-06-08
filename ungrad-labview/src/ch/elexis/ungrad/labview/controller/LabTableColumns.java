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
package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.ungrad.labview.Preferences;
import ch.elexis.ungrad.labview.model.LabResultsSheet;
import ch.rgw.tools.TimeTool;

/**
 * Host for all Columns in the Table
 * 
 * @author gerry
 *
 */
public class LabTableColumns {
	public static final int COL_LATEST = 2;
	public static final int COL_RECENT = 3;
	public static final int COL_LASTYEAR = 4;
	public static final int COL_OLDER = 5;
	private final int num = COL_OLDER+1;

	private TreeViewerColumn[] cols;
	private LabResultsSheet sheet;
	private Font smallerFont;

	public LabTableColumns(LabResultsSheet sheet, TreeViewer tv) {
		this.sheet = sheet;
		cols = new TreeViewerColumn[num];
		for (int i = 0; i < num; i++) {
			cols[i] = new TreeViewerColumn(tv, SWT.NONE);
			cols[i].getColumn().setResizable(true);
		}
		Display display = Display.getDefault();
		FontData[] fontData = getDefaultFont().getFontData();
		for (int i = 0; i < fontData.length; ++i) {
			float h = fontData[i].getHeight() * 4 / 5;
			fontData[i].setHeight(Math.round(h));
		}
		smallerFont = new Font(display, fontData);
	}

	public void dispose() {
		smallerFont.dispose();
	}

	public int getColumnWidth(int column) {
		if (column < 0 || column > cols.length) {
			return 0;
		}
		return cols[column].getColumn().getWidth();
	}

	public LabResultsSheet getLabResultsSheet() {
		return sheet;
	}

	public Font getDefaultFont() {
		return UiDesk.getFont(ch.elexis.core.constants.Preferences.USR_DEFAULTFONT);
	}

	public Font getSmallerFont() {
		return smallerFont;
	}

	public void reload(LabContentProvider lcp) {
		cols[0].getColumn().setText("Parameter");
		cols[0].getColumn().setWidth(150);
		cols[0].setLabelProvider(new ItemTextLabelProvider());
		cols[1].getColumn().setText("Normbereich");
		cols[1].getColumn().setWidth(150);
		cols[1].setLabelProvider(new ItemRangeLabelProvider());
		String mode = Preferences.cfg.get(Preferences.MODE, "compact");
		switch (mode) {
		case "compact":
			reloadCompact(lcp);
			break;
		}
	}

	private void reloadCompact(LabContentProvider lcp) {
		TimeTool[] dates = lcp.lrs.getDates();
		if (dates != null && dates.length>0) {
			TimeTool date=dates[dates.length - 1];
			defineColumn(cols[COL_LATEST],date.toString(TimeTool.DATE_GER),100,new LatestResultLabelProvider(date));
		} else {
			defineColumn(cols[COL_LATEST],"",10,new NullLabelProvider());
		}
		defineColumn(cols[COL_RECENT],"letzter Monat",130,new CondensedViewLabelProvider(this,COL_RECENT));
		defineColumn(cols[COL_LASTYEAR],"letzte 12 Monat",130,new CondensedViewLabelProvider(this,COL_LASTYEAR));
		defineColumn(cols[COL_OLDER],"älter",130,new CondensedViewLabelProvider(this,COL_OLDER));
	}
	
	private void defineColumn(TreeViewerColumn tvc, String title, int width, CellLabelProvider lp){
		tvc.getColumn().setText(title);
		tvc.getColumn().setWidth(width);
		tvc.setLabelProvider(lp);
	}
}

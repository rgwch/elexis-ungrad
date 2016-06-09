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
import org.eclipse.swt.widgets.TreeColumn;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.ungrad.labview.model.LabResultsSheet;
import ch.rgw.tools.TimeTool;

/**
 * Host for all Columns in the Table
 * 
 * @author gerry
 *
 */
public class LabSummaryTableColumns {
	public static final int COL_LATEST = 2;
	public static final int COL_RECENT = 3;
	public static final int COL_LASTYEAR = 4;
	public static final int COL_OLDER = 5;
	private final int num = COL_OLDER+1;
	private final String[] captions={"Parameter","Referenz","","letzter Monat","letzte 12 Monate","älter"};
	private int[] widths={150,150,100,130,130,130};
	private LatestResultLabelProvider lrlp=new LatestResultLabelProvider();
	private CellLabelProvider[] labelProviders={
			new ItemTextLabelProvider(),
			new ItemRangeLabelProvider(),
			lrlp,
			new CondensedViewLabelProvider(this, COL_RECENT),
			new CondensedViewLabelProvider(this, COL_LASTYEAR),
			new CondensedViewLabelProvider(this, COL_OLDER)
	};

	TreeViewerColumn[] cols;
	private LabResultsSheet sheet;
	private Font smallerFont;

	public LabSummaryTableColumns(LabResultsSheet sheet, TreeViewer tv) {
		this.sheet = sheet;
		Display display = Display.getDefault();
		FontData[] fontData = getDefaultFont().getFontData();
		for (int i = 0; i < fontData.length; ++i) {
			float h = fontData[i].getHeight() * 3f / 4f;
			fontData[i].setHeight(Math.round(h));
		}
		smallerFont = new Font(display, fontData);
		
		cols = new TreeViewerColumn[num];
		
		for (int i = 0; i < num; i++) {
			cols[i] = new TreeViewerColumn(tv, SWT.NONE);
			defineColumn(cols[i],captions[i],widths[i],labelProviders[i]);
		}

	
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

	public void reload(LabSummaryContentProvider lcp) {
		TimeTool[] dates = lcp.lrs.getDates();
		if (dates != null && dates.length>0) {
			TimeTool date=dates[dates.length - 1];
			cols[COL_LATEST].getColumn().setText(date.toString(TimeTool.DATE_GER));
			lrlp.setDate(date);
		} else {
			cols[COL_LATEST].getColumn().setText("-");
			lrlp.setDate(null);
		}
	}
	
	private void defineColumn(TreeViewerColumn tvc, String title, int width, CellLabelProvider lp){
		TreeColumn tc=tvc.getColumn();
		tc.setText(title);
		tc.setWidth(width);
		tc.setResizable(true);
		tvc.setLabelProvider(lp);
	}
}

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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.ungrad.labview.Preferences;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

/**
 * Host for all Columns in the Table
 * 
 * @author gerry
 *
 */
public class LabTableColumns {
	public static final int COL_RECENT = 2;
	public static final int COL_LASTYEAR = 3;
	public static final int COL_OLDER = 4;

	private TableViewerColumn[] cols;
	private LabResultsSheet sheet;
	private Font smallerFont;

	public LabTableColumns(LabResultsSheet sheet, TableViewer tv, int num) {
		this.sheet = sheet;
		cols = new TableViewerColumn[num];
		for (int i = 0; i < num; i++) {
			cols[i] = new TableViewerColumn(tv, SWT.NONE);
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
		cols[COL_RECENT].getColumn().setText("aktuell");
		cols[COL_RECENT].getColumn().setWidth(130);
		cols[COL_RECENT].setLabelProvider(new CondensedViewLabelProvider(this, COL_RECENT));
		cols[COL_LASTYEAR].getColumn().setText("letzte 12 Monate");
		cols[COL_LASTYEAR].getColumn().setWidth(130);
		cols[COL_LASTYEAR].setLabelProvider(new CondensedViewLabelProvider(this, COL_LASTYEAR));
		cols[COL_OLDER].getColumn().setText("älter");
		cols[COL_OLDER].getColumn().setWidth(130);
		cols[COL_OLDER].setLabelProvider(new CondensedViewLabelProvider(this, COL_OLDER));
		for (int i = 5; i < cols.length; i++) {
			cols[i].getColumn().setWidth(0);
			cols[i].setLabelProvider(new NullLabelProvider());
		}
	}
}

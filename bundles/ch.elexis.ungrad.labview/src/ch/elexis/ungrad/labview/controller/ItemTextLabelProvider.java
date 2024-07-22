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

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;

/**
 * LabelProvider for the Item title
 * 
 * @author gerry
 *
 */
public class ItemTextLabelProvider extends StyledCellLabelProvider {
	private Color headingsBG = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
	private Color headingsFG = UiDesk.getColor(UiDesk.COL_BLUE);
	private Color red = Display.getDefault().getSystemColor(SWT.COLOR_RED);

	@Override
	public void update(ViewerCell cell) {
		if (cell.getElement() instanceof LabResultsRow) {
			LabResultsRow row = (LabResultsRow) cell.getElement();
			Item item = row.getItem();
			String titel = item.get("titel");
			if (row.hasRelevantResults()) {
				StyleRange sr = new StyleRange(0, titel.length(), red, null);
				cell.setStyleRanges(new StyleRange[] { sr });
			}
			cell.setText(titel);
			super.update(cell);
		} else if (cell.getElement() instanceof Item) {
			Item item = (Item) cell.getElement();
			cell.setText(item.get("titel"));
			super.update(cell);
		} else {
			String titel = (String) cell.getElement();
			cell.setBackground(headingsBG);
			cell.setForeground(headingsFG);
			StyleRange sr = new StyleRange(0, titel.length(), null, null, SWT.BOLD);
			cell.setStyleRanges(new StyleRange[] { sr });
			cell.setText(titel);
			super.update(cell);
		}
	}

}

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

import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.tools.TimeTool;

public class DateResultLabelProvider extends StyledCellLabelProvider {

	TimeTool myDate;
	Controller controller;
	Color red, black, gray;

	public DateResultLabelProvider(Controller ctl) {
		this.controller = ctl;
		gray = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
		red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		black = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	}

	public DateResultLabelProvider(Controller ctl, TimeTool date) {
		this(ctl);
		myDate = date;
	}

	private void setDate(Controller ctl, TimeTool date) {
		controller = ctl;
		myDate = date;
	}

	private void styleCell(ViewerCell cell, Item item, Result result) {
		if (result == null) {
			cell.setText("");
		} else {
			String display = result.get("resultat");
			if (display == null) {
				display = "";
			}
			if (controller.getLRS().isPathologic(item, result)) {
				// System.out.println(item.dump());
				// System.out.println(result.dump());
				StyleRange sr = new StyleRange(0, display.length(), red, cell.getBackground());
				cell.setStyleRanges(new StyleRange[] { sr });
			}
			cell.setText(display);
		}
	}

	@Override
	public void update(ViewerCell cell) {
		if ((cell.getElement() instanceof String) || (myDate == null)) {
			// labgroup or invalid. dont't text
			cell.setBackground(gray);

		} else if (cell.getElement() instanceof LabResultsRow) {
			LabResultsRow row = (LabResultsRow) cell.getElement();
			styleCell(cell, row.getItem(), row.get(myDate));
		} else if (cell.getElement() instanceof Item) {
			Item item = (Item) cell.getElement();
			styleCell(cell, item, controller.getLRS().getResultForDate(item, myDate));

		}
		super.update(cell);
	}

}

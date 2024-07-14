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

package ch.elexis.ungrad.labview.controller.smart;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.tools.TimeTool;

public class SmartSummaryLabelProvider extends StyledCellLabelProvider {
	Controller ctl;
	TimeTool limit;
	Color gray = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
	Color red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	Color black = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	
	public SmartSummaryLabelProvider(Controller ctl, TimeTool before){
		this.ctl = ctl;
		limit = before;
	}
	
	@Override
	public void update(ViewerCell cell){
		if (cell.getElement() instanceof LabResultsRow) {
			LabResultsRow lr = (LabResultsRow) cell.getElement();
			Result[] minmax = lr.getBoundsBefore(limit);
			StringBuilder result = new StringBuilder("");
			if (minmax != null && minmax[0] != null) {
				String lower = minmax[0].get("resultat");
				StyleRange[] sr = new StyleRange[2];
				sr[0] =
					new StyleRange(0, lower.length(), cell.getForeground(), cell.getBackground());
				sr[1] = new StyleRange(0, 0, cell.getForeground(), cell.getBackground());
				if (ctl.getLRS().isPathologic(lr.getItem(), minmax[0])) {
					sr[0].foreground = red;
				}
				result.append(lower);
				if (!minmax[0].equals(minmax[1]) && minmax[1] != null) {
					String upper = minmax[1].get("resultat");
					sr[1].start = lower.length() + 1;
					sr[1].length = upper.length();
					if (ctl.getLRS().isPathologic(lr.getItem(), minmax[1])) {
						sr[1].foreground = red;
					}
					result.append("-").append(upper);
				}
				cell.setStyleRanges(sr);
			}
			cell.setText(result.toString());
			super.update(cell);
		} else {
			cell.setBackground(gray);
		}
	}
	
}

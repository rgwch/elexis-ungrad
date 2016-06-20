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
package ch.elexis.ungrad.labview.controller.full;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.tools.TimeTool;

class FullLabelProvider extends StyledCellLabelProvider {
	FullViewController fvc;
	TimeTool myDate;
	Color black, red;
	
	public FullLabelProvider(FullViewController fvc, TimeTool date){
		this.fvc = fvc;
		myDate = date;
		black = UiDesk.getColor(UiDesk.COL_BLACK);
		red = UiDesk.getColor(UiDesk.COL_RED);
		
	}
	
	public void setDate(TimeTool date){
		myDate = date;
	}
	
	@Override
	public void update(ViewerCell cell){
		
		if (cell.getElement() instanceof Item) {
			Item item = (Item) cell.getElement();
			Result result = fvc.getLRS().getResultForDate(item, myDate);
			if (result == null) {
				cell.setText("");
			} else {
				String res = result.get("resultat");
				if (fvc.getLRS().isPathologic(item, result)) {
					StyleRange sr = new StyleRange(0, res.length(), red, cell.getBackground());
					cell.setStyleRanges(new StyleRange[] {
						sr
					});
				}
				cell.setText(res);
			}
		} else {
			cell.setText("");
		}
		super.update(cell);
	}
}
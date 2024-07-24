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
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.model.IPatient;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.Util;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

/**
 * LabelProvider for the norm-Range column
 * 
 * @author gerry
 *
 */
public class ItemRangeLabelProvider extends CellLabelProvider {
	private LabResultsSheet lrs;

	public ItemRangeLabelProvider(LabResultsSheet lrs) {
		this.lrs = lrs;
	}

	public ItemRangeLabelProvider() {
	}

	@Override
	public void update(ViewerCell cell) {
		if (cell.getElement() instanceof LabResultsRow) {
			LabResultsRow results = (LabResultsRow) cell.getElement();
			Item item = results.getItem();
			IPatient pat = results.getPatient();
			if (Util.isFemale(pat)) {
				cell.setText(item.get("RefFrauOrTx"));
			} else {
				cell.setText(item.get("refMann"));
			}
		} else if (cell.getElement() instanceof Item) {
			cell.setText(lrs.getNormRange((Item) cell.getElement()));
		} else {
			cell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
			cell.setText("");
		}
	}

}

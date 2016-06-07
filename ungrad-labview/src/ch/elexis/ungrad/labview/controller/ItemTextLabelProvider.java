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

import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;

/**
 * LabelProvider for the Item title
 * @author gerry
 *
 */
public class ItemTextLabelProvider extends CellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		if(cell.getElement() instanceof LabResultsRow){
			LabResultsRow row=(LabResultsRow)cell.getElement();
			Item item=row.getItem();
			cell.setText(item.titel);
		}else{
			cell.setText("?");
		}
	}

}

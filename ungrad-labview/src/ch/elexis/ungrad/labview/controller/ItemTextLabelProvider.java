package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;

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

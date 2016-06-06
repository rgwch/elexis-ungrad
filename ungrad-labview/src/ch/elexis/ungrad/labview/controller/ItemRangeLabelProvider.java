package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import ch.elexis.core.types.Gender;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;

public class ItemRangeLabelProvider extends CellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		LabResultsRow results=(LabResultsRow) cell.getElement();
		Item item=results.getItem();
		Patient pat=results.getPatient();
		if(pat.getGender()==Gender.FEMALE){
			cell.setText(item.refFrauOrTx);
		}else{
			cell.setText(item.refMann);
		}
	}

}

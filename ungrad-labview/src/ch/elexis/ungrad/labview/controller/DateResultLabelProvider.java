package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.tools.TimeTool;

public class DateResultLabelProvider extends CellLabelProvider {

	TimeTool myDate;
	
	void setDate(TimeTool date){
		myDate=date;
	}
	
	@Override
	public void update(ViewerCell cell) {
		if (myDate == null) {
			cell.setText("");
		} else {
			if (cell.getElement() instanceof LabResultsRow) {
				LabResultsRow row = (LabResultsRow) cell.getElement();
				Result result = row.get(myDate);
				if (result != null) {
					cell.setText(result.get("resultat"));
				}
			} else {
				cell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
			}

		}

	}

}

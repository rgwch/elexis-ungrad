package ch.elexis.ungrad.labview.controller.full;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.tools.TimeTool;

class FullLabelProvider extends StyledCellLabelProvider {
	FullViewController fvc;
	TimeTool myDate;
	
	public FullLabelProvider(FullViewController fvc, TimeTool date){
		this.fvc = fvc;
		myDate = date;
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
				cell.setText(result.get("resultat"));
			}
		} else {
			cell.setText("");
		}
		super.update(cell);
	}
}
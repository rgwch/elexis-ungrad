package ch.elexis.ungrad.labview.controller.full;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.ungrad.labview.model.Item;

class FullLabelProvider extends TableLabelProvider {
	@Override
	public String getColumnText(Object element, int columnIndex) {
		
		if(element instanceof Item){
			int dateIndex=columnIndex-2;
		}
		if(columnIndex==0){
			return ((Item)element).get("title");
		}else{
			return "";
		}
	}
}
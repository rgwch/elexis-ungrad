package ch.elexis.ungrad.labview.controller.full;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.ungrad.labview.model.Result;

public class LabResultEditor extends TextCellEditor {
	Result result;
	
	public LabResultEditor(Composite parent) {
		super(parent);
	}
	
	@Override
	protected Object doGetValue() {
		String value=text.getText();
		result.set("resultat", value);
		return result;
	}

	@Override
	protected void doSetFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doSetValue(Object value) {
		if(value instanceof Result){
			result=(Result) value;
		}else{
			value=new Result(0f);
		}
	}

}

package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class LabInputEditingSupport extends EditingSupport {
	CellEditor ced;
	
	public LabInputEditingSupport(TableViewer tv) {
		super(tv);
		ced=new TextCellEditor(tv.getTable());
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return ced;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		// TODO Auto-generated method stub

	}

}

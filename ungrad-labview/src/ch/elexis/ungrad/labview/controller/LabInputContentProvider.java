package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.ungrad.labview.model.LabResultsSheet;

class LabInputContentProvider implements IStructuredContentProvider {
	LabResultsSheet lrs;
	
	public LabInputContentProvider(LabResultsSheet lrs) {
		this.lrs=lrs;
	}
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	@Override
	public void dispose() {}

	@Override
	public Object[] getElements(Object inputElement) {
		return lrs.getItems();
	}
}
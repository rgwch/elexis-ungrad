package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.ungrad.labview.model.LabResultsSheet;

class FullContentProvider implements ITreeContentProvider {
	LabResultsSheet lrs;
	
	public FullContentProvider(LabResultsSheet lrs) {
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
	@Override
	public Object[] getChildren(Object parentElement){
		return new Object[0];
	}
	@Override
	public Object getParent(Object element){
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean hasChildren(Object element){
		return false;
	}
}
package ch.elexis.ungrad.labview.controller.full;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.ungrad.labview.model.Item;
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
		return lrs.getAllGroups();
		
	}
	@Override
	public Object[] getChildren(Object parentElement){
		return lrs.getAllItemsForGroup(parentElement.toString());
	}
	
	@Override
	public Object getParent(Object element){
		if(element instanceof Item){
			return ((Item)element).get("gruppe");
		}else{
			return null;
		}
	}
	@Override
	public boolean hasChildren(Object element){
		return true;
	}
}
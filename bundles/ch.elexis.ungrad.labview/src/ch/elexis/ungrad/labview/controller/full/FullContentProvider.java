/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/
package ch.elexis.ungrad.labview.controller.full;

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
		return element instanceof String;
	}
}
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

package ch.elexis.ungrad.labview.controller.smart;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

public class SmartContentProvider implements ITreeContentProvider {
	LabResultsSheet lrs;
	
	public SmartContentProvider(LabResultsSheet lrs){
		this.lrs = lrs;
	}
	
	@Override
	public void dispose(){}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		Object[] ret = lrs.getGroups();
		if (ret == null) {
			return new String[0];
		} else {
			return ret;
		}
	}
	
	@Override
	public Object[] getChildren(Object parentElement){
		return lrs.getRowsForGroup((String) parentElement);
	}
	
	@Override
	public Object getParent(Object element){
		if (element instanceof LabResultsRow) {
			return ((LabResultsRow) element).getItem().get("gruppe");
		} else {
			return null;
		}
	}
	
	@Override
	public boolean hasChildren(Object element){
		return element instanceof String;
	}
	
}

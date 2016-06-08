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
package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

/**
 * The ContentProvider for the condensed view
 * 
 * @author gerry
 *
 */
public class LabContentProvider implements ITreeContentProvider {
	LabResultsSheet lrs = new LabResultsSheet();

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	void setPatient(Patient pat) throws ElexisException {
		lrs.setPatient(pat);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		Object[] groups = lrs.getGroups();
		if (groups == null) {
			return new String[0];
		} else {
			return groups;
		}
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getChildren(Object element) {
		return lrs.getRowsForGroup((String) element);
	}

	@Override
	public Object getParent(Object element) {
		LabResultsRow row = (LabResultsRow) element;
		return row.getItem().gruppe;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof String) {
			return true;
		} else {
			return false;
		}
	}

}

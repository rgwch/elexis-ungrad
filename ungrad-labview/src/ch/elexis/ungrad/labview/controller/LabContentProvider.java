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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

/**
 * The ContentProvider for the condensed view
 * @author gerry
 *
 */
public class LabContentProvider implements IStructuredContentProvider {
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
		LabResultsRow[] items = lrs.getLabResults();
		if (items == null) {
			return new LabResultsRow[0];
		} else {
			return items;
		}
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

}

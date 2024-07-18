/*******************************************************************************
 * Copyright (c) 2016-2024 by G. Weirich
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

package ch.elexis.ungrad.lucinda.controller;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		List<Map> values = (List) inputElement;
		Object[] ret = new Object[values.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = values.get(i);
		}
		return ret;
	}

}

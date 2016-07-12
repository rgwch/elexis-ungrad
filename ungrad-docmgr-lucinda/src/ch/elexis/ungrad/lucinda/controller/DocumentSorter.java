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

package ch.elexis.ungrad.lucinda.controller;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.rgw.tools.TimeTool;

public class DocumentSorter extends ViewerSorter {
	int index;
	boolean bIncreasing;
	boolean isDate;
	
	public DocumentSorter(int index, boolean bIncreasing, boolean isDate){
		this.index = index;
		this.bIncreasing = bIncreasing;
		this.isDate = isDate;
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2){
		String s1 = ((TableLabelProvider) ((TableViewer) viewer).getLabelProvider())
			.getColumnText(e1, index).toLowerCase();
		String s2 = ((TableLabelProvider) ((TableViewer) viewer).getLabelProvider())
			.getColumnText(e2, index).toLowerCase();
		if (isDate) {
			TimeTool t1 = new TimeTool(s1);
			TimeTool t2 = new TimeTool(s2);
			if (bIncreasing) {
				return (t1.compareTo(t2));
			} else {
				return (t2.compareTo(t1));
			}
		} else {
			if (bIncreasing) {
				return s1.compareTo(s2);
			} else {
				return s2.compareTo(s1);
			}
		}
	}
	
}
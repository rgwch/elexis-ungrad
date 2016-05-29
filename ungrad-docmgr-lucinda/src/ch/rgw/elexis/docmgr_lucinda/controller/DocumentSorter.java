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

package ch.rgw.elexis.docmgr_lucinda.controller;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;

public class DocumentSorter extends ViewerSorter {
	int index;
	boolean bIncreasing;
	
	public DocumentSorter(int index, boolean bIncreasing){
		this.index=index;
		this.bIncreasing=bIncreasing;
	}
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		String s1=((TableLabelProvider)((TableViewer)viewer).getLabelProvider()).getColumnText(e1, index).toLowerCase();
		String s2=((TableLabelProvider)((TableViewer)viewer).getLabelProvider()).getColumnText(e2, index).toLowerCase();
		if(bIncreasing){
			return s1.compareTo(s2);
		}else{
			return s2.compareTo(s1);
		}
	}

	
}
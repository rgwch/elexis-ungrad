/*******************************************************************************
 * Copyright (c) 2007-2016, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/package ch.berchtold.emanuel.privatrechnung.model;

import org.eclipse.jface.viewers.TreeViewer;

import ch.berchtold.emanuel.privatrechnung.data.Leistung;
import ch.elexis.core.ui.actions.TreeDataLoader;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.Tree;

public class LeistungenLoader extends TreeDataLoader {
	public LeistungenLoader(CodeSelectorFactory csf, CommonViewer cv,
		Query<? extends PersistentObject> qbe, String parentField){
		super(cv, qbe, parentField, "Kuerzel");
	}
	
	public void updateChildCount(Object element, int currentChildCount){
		int num = 0;
		if (element instanceof Tree) {
			@SuppressWarnings("unchecked")
			Tree<Leistung> t = (Tree<Leistung>) element;
			if (!t.hasChildren()) {
				qbe.clear();
				qbe.add(parentColumn, "=", t.contents.get("Kuerzel"));
				applyQueryFilters();
				for (PersistentObject po : qbe.execute()) {
					new Tree<Leistung>(t, (Leistung) po);
				}
			}
			num = t.getChildren().size();
		} else {
			num = root.getChildren().size();
		}
		((TreeViewer) cv.getViewerWidget()).setChildCount(element, num);
	}
	
}
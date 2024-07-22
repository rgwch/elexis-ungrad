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

package ch.elexis.ungrad.lucinda.view;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.ungrad.lucinda.Activator;

public class LucindaMessages extends ViewPart {
	private TreeViewer tv;

	public LucindaMessages() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		tv = new TreeViewer(parent);
		tv.setContentProvider(new ITreeContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof Map) {
					return true;
				} else {
					return false;
				}
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return Activator.getDefault().getMessages().toArray();
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				Map doc = (Map) parentElement;
				Set<Entry<String, Object>> entries = doc.entrySet();
				return entries.toArray();
			}
		});

		tv.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof Map) {
					return (String) ((Map) element).get("status"); //$NON-NLS-1$
				} else if (element instanceof Entry) {
					Entry e = (Entry) element;
					return e.getKey() + ": " + e.getValue(); //$NON-NLS-1$
				} else {
					return "?"; //$NON-NLS-1$
				}

			}

		});

		tv.setInput(Activator.getDefault().getMessages());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}

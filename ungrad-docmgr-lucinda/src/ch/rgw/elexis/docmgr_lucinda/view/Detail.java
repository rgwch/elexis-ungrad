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

package ch.rgw.elexis.docmgr_lucinda.view;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.rgw.elexis.docmgr_lucinda.Preferences;
import ch.rgw.tools.StringTool;

/**
 * The Detail-Subview shows metadata of the document selected in the Master-Subview.
 * @author gerry
 *
 */
public class Detail extends Composite {
	TableViewer tv;
	private String[] exclusions;;
	
	public Detail(Composite parent){
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		tv = new TableViewer(this, SWT.NONE);
		TableViewerColumn tvc1 = new TableViewerColumn(tv, SWT.NONE);
		TableColumn tc1 = tvc1.getColumn();
		tv.getTable().setHeaderVisible(true);
		tv.getTable().setLinesVisible(true);
		tc1.setWidth(200);
		tc1.setText("key"); //$NON-NLS-1$
		
		TableViewerColumn tvc2 = new TableViewerColumn(tv, SWT.NONE);
		TableColumn tc2 = tvc2.getColumn();
		tc2.setWidth(100);
		tc2.setText("value"); //$NON-NLS-1$
		
		tv.setContentProvider(new IStructuredContentProvider() {
			/*
			 * We'll recheck exclusions on every changed object, in case the user edited the list
			 */
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				exclusions = CoreHub.localCfg.get(Preferences.EXCLUDEMETA, "").split(","); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			@Override
			public void dispose(){}
			
			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement){
				Map<String, Object> el = (Map<String, Object>) inputElement;
				Stream<Entry<String, Object>> filtered =
					el.entrySet().stream().filter(e -> isNotExcluded(e));
				return filtered.toArray();
			}
		});
		tv.setLabelProvider(new TableLabelProvider() {
			
			@SuppressWarnings("unchecked")
			@Override
			public String getColumnText(Object element, int columnIndex){
				Entry<String, Object> en = (Entry<String, Object>) element;
				if (columnIndex == 0) {
					return en.getKey();
				} else {
					return en.getValue().toString();
				}
			}
			
		});
		
	}
	
	public void setInput(Object input){
		tv.setInput(input);
	}
	
	/* show only items not in the exclude-List*/
	private boolean isNotExcluded(Entry<String, Object> e){
		if (exclusions == null) {
			return true;
		}
		return (StringTool.getIndex(exclusions, e.getKey()) == -1);
		
	}
}

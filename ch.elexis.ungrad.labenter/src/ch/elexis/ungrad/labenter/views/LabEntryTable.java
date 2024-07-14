/*******************************************************************************
 * Copyright (c) 2018 by G. Weirich
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

package ch.elexis.ungrad.labenter.views;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.LabItem;
import ch.elexis.ungrad.labenter.preferences.PreferenceConstants;
import ch.rgw.io.Settings;

public class LabEntryTable {
	TableViewer viewer;
	Element[] elements;
	
	LabEntryTable(Composite parent){
		Settings settings = CoreHub.globalCfg;
		String[] itemlist = settings.getStringArray(PreferenceConstants.P_ITEMS);
		elements = new Element[itemlist.length];
		for (int i = 0; i < itemlist.length; i++) {
			elements[i] = new Element(itemlist[i]);
		}
		
		Table table = new Table(parent, SWT.NONE /* SWT.FULL_SELECTION */);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(SWTHelper.getFillGridData());
		viewer = new TableViewer(table);
		createColumns(parent, viewer);
		
		TableViewerEditor.create(viewer, new ColumnViewerEditorActivationStrategy(viewer),
			ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
		
		viewer.setContentProvider(new ArrayContentProvider() {
			
			@Override
			public Object[] getElements(Object inputElement){
				return (Object[]) inputElement;
			}
			
		});
		viewer.getTable().addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e){
				if (e.keyCode == SWT.CR) {
					int idx = viewer.getTable().getSelectionIndex();
					viewer.editElement(viewer.getElementAt(idx), 1);
				}
			}
			
		});
		viewer.setInput(elements);
	}
	
	private TableViewerColumn createColumn(String title, int bound, int colNumber){
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		return viewerColumn;
	}
	
	private void createColumns(final Composite parent, final TableViewer viewer){
		TableViewerColumn tvc = createColumn("Parameter", 300, 0);
		tvc.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element){
				return ((Element) element).label;
			}
			
		});
		tvc = createColumn("Wert", 50, 1);
		tvc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public void update(ViewerCell cell){
				cell.setText(((Element) cell.getElement()).value);
			}
			
		});
		tvc.setEditingSupport(new LabValueEditingSupport(viewer));
	}
	
	class LabValueEditingSupport extends EditingSupport {
		private final TableViewer viewer;
		private final CellEditor editor;
		
		public LabValueEditingSupport(TableViewer viewer){
			super(viewer);
			this.viewer = viewer;
			this.editor = new TextCellEditor(viewer.getTable());
			
			this.editor.getControl().addKeyListener(new KeyAdapter() {
				
				@Override
				public void keyPressed(KeyEvent e){
					int idx = viewer.getTable().getSelectionIndex();
					switch (e.keyCode) {
					case SWT.CR:
					case SWT.TAB:
					case SWT.ARROW_DOWN:
						if (idx < elements.length - 2) {
							e.doit = true;
							viewer.editElement(viewer.getElementAt(idx + 1), 1);
						}
						break;
					case SWT.ARROW_UP:
						if (idx > 0) {
							e.doit = true;
							viewer.editElement(viewer.getElementAt(idx - 1), 1);
						}
						break;
					}
					//super.keyPressed(e);
				}
				
			});
		}
		
		@Override
		protected CellEditor getCellEditor(Object element){
			return editor;
		}
		
		@Override
		protected boolean canEdit(Object element){
			return true;
		}
		
		@Override
		protected Object getValue(Object element){
			return ((Element) element).value;
		}
		
		@Override
		protected void setValue(Object element, Object userInputValue){
			((Element) element).value = String.valueOf(userInputValue);
			viewer.update(element, null);
			/*
			 * int idx=viewer.getTable().getSelectionIndex(); if(idx<elements.length-1) {
			 * Element nextelem=elements[idx+1]; viewer.editElement(nextelem, 1); }
			 */
		}
	}
	
	class Element {
		Element(String itemId){
			this.item = LabItem.load(itemId);
			this.label = item.getKuerzel() + " (" + item.getReferenceMale() + ") " + item.getUnit();
			this.value = "";
		}
		
		LabItem item;
		String label;
		String value;
	}
}

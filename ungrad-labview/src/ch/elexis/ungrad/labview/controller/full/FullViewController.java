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
package ch.elexis.ungrad.labview.controller.full;

import java.util.logging.Logger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.LabItem;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsSheet;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.tools.TimeTool;

public class FullViewController implements IObserver {
	TreeViewer tvFull;
	Controller controller;
	FullDisplayTreeColumns fdtc;
	TreeViewerFocusCellManager focusManager;
	TextCellEditor tce;
	private IAction organizeItemAction, toGroupAction;
	private Logger log=Logger.getLogger(getClass().getName());
	
	public FullViewController(Controller parent){
		controller = parent;
		organizeItemAction = new Action("Organisieren") {
			{
				setImageDescriptor(Images.IMG_ARROWDOWNTORECT.getImageDescriptor());
			}
			
			@Override
			public void run(){
				SWTHelper.alert("hallo", "So");
			}
			
		};
		toGroupAction = new Action("Zu Gruppe...") {
			{
				setImageDescriptor(Images.IMG_MOVETOUPPERLIST.getImageDescriptor());
			}
			
			@Override
			public void run(){
				IStructuredSelection sel = (IStructuredSelection) tvFull.getSelection();
				int num = sel.size();
				String[] allGroups = getLRS().getAllGroups();
				
				InputDialog dlg =
					new InputDialog(tvFull.getTree().getShell(), "LaborItems verschieben",
						"Bitte Zielgruppe angeben (" + num + " Items gewählt)", "ZZ",
						new IInputValidator() {
						
					@Override
					public String isValid(String newText){
						int hits = 0;
						for (String group : allGroups) {
							if (group.startsWith(newText)) {
								hits++;
							}
						}
						if (hits == 0) {
							return "Gruppe nicht vorhanden";
						} else if (hits == 1) {
							return null;
						} else {
							return "Gruppe nicht eindeutig.";
						}
					}
				});
				if (dlg.open() == Window.OK) {
					String result = dlg.getValue();
					for(String group:allGroups){
						if(group.startsWith(result)){
							doMove(sel.toArray(),group);
						}
					}
				}
			}
		};
		
	}
	private void doMove(Object[] objects, String group){
		for(Object o:objects){
			if(o instanceof Item){
				Item item=(Item)o;
				String id=item.get("id");
				LabItem li=LabItem.load(id);
				if(li!=null && li.isValid()){
					li.set(LabItem.GROUP, group);
				}
			}
		}
		try{
			getLRS().reload();
		}catch(ElexisException eex){
			log.severe("could not reload LabItems "+eex.getMessage());
		}
	}
	public LabResultsSheet getLRS(){
		return controller.getLRS();
	}
	
	public Control createControl(Composite parent){
		tvFull = new TreeViewer(parent);
		Tree tree = tvFull.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tvFull.setContentProvider(new FullContentProvider(controller.getLRS()));
		fdtc = new FullDisplayTreeColumns(this);
		tce = new TextCellEditor(tree);
		focusManager = new TreeViewerFocusCellManager(tvFull, new FocusCellHighlighter(tvFull) {});
		controller.getLRS().addObserver(this);
		tvFull.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				boolean bEnable = !tvFull.getSelection().isEmpty();
				organizeItemAction.setEnabled(bEnable);
				toGroupAction.setEnabled(bEnable);
			}
		});
		tvFull.getTree().setMenu(createContextMenu(tvFull.getTree()));
		return tree;
	}
	
	@Override
	public void signal(Object message){
		if (message instanceof Patient) {
			fdtc.reload();
			tvFull.setInput(message);
		}
	}
	
	private Menu createContextMenu(Control parent){
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager){
				mgr.add(organizeItemAction);
				mgr.add(toGroupAction);
			}
		});
		return mgr.createContextMenu(parent);
	}
	
	EditingSupport createEditingSupportFor(TreeViewerColumn tvc, TimeTool colDate){
		return new EditingSupport(tvFull) {
			
			@Override
			protected void setValue(Object element, Object value){
				if (element instanceof Item) {
					Result result = getLRS().getResultForDate((Item) element, colDate);
					if (result == null) {
						result = new Result(0f);
					}
					result.set("resultat", (String) value);
					result.set("ItemID", ((Item) element).get("ID"));
					result.set("Datum", colDate.toString(TimeTool.DATE_COMPACT));
					result.set("Zeit", colDate.toString(TimeTool.TIME_SMALL));
					getLRS().addResult(result);
					tvFull.update(element, null);
				}
			}
			
			@Override
			protected Object getValue(Object element){
				if (element instanceof Item) {
					Result result = getLRS().getResultForDate((Item) element, colDate);
					if (result == null) {
						result = new Result(0f);
					}
					return result.get("resultat");
				} else {
					return "";
				}
			}
			
			@Override
			protected CellEditor getCellEditor(Object element){
				return tce;
			}
			
			@Override
			protected boolean canEdit(Object element){
				return element instanceof Item;
			}
		};
	}
}

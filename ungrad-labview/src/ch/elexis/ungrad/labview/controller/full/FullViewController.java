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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.Action;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabMapping;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsSheet;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class FullViewController implements IObserver {
	TreeViewer tvFull;
	Controller controller;
	FullDisplayTreeColumns fdtc;
	TreeViewerFocusCellManager focusManager;
	TextCellEditor tce;
	private Action organizeItemAction, toGroupAction, combineAction, sortAction;
	private org.slf4j.Logger log = LoggerFactory.getLogger(this.getClass());
	
	public FullViewController(Controller parent){
		controller = parent;
		organizeItemAction =
			new RestrictedAction(AccessControlDefaults.DELETE_LABITEMS, "Eigenschaften") {
				{
					setImageDescriptor(Images.IMG_ARROWDOWNTORECT.getImageDescriptor());
					setToolTipText("Eigenschaften des Labor Items ändern");
				}
				
				@Override
				public void doRun(){
					IStructuredSelection sel = (IStructuredSelection) tvFull.getSelection();
					Item item = (Item) sel.getFirstElement();
					LabItemPropertiesDlg dlg = new LabItemPropertiesDlg(item);
					dlg.create();
					if (dlg.open() == Window.OK) {
						reload();
					}
				}
				
			};
		toGroupAction =
			new RestrictedAction(AccessControlDefaults.DELETE_LABITEMS, "Zu Gruppe...") {
				{
					setImageDescriptor(Images.IMG_MOVETOUPPERLIST.getImageDescriptor());
				}
				
				@Override
				public void doRun(){
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
						for (String group : allGroups) {
							if (group.startsWith(result)) {
								doMove(sel.toArray(), group);
							}
						}
						reload();
					}
				}
			};
		combineAction = new Action("Items zusammenführen") {
			{
				setImageDescriptor(Images.IMG_MOVETOLOWERLIST.getImageDescriptor());
			}
			
			@Override
			public void run(){
				IStructuredSelection sel = (IStructuredSelection) tvFull.getSelection();
				if (SWTHelper.askYesNo("Items kombinieren",
					"Wollen Sie wirklich diese " + sel.size() + " Items kombinieren?")) {
					doCombine(sel.toArray());
					reload();
				}
			}
		};
		sortAction = new RestrictedAction(AccessControlDefaults.DELETE_LABITEMS, "Neu sortieren") {
			{
				setImageDescriptor(Images.IMG_SORT_STAR.getImageDescriptor());
				setToolTipText("Items dieser Gruppe neu sortieren");
			}
			
			@Override
			public void doRun(){
				String group =
					(String) ((IStructuredSelection) tvFull.getSelection()).getFirstElement();
				Item[] items = getLRS().getAllItemsForGroup(group);
				Arrays.sort(items, new Comparator<Item>() {
					
					@Override
					public int compare(Item o1, Item o2){
						int n = o1.get("titel").compareTo(o2.get("titel"));
						if (n == 0) {
							n = o2.get("kuerzel").compareTo(o2.get("kuerzel"));
						}
						return n;
					}
				});
				
				for (int i = 0; i < items.length; i++) {
					String prio =
						StringTool.pad(StringTool.LEFT, '0', Integer.toString(10 * i + 10), 3);
					LabItem li = LabItem.load(items[i].get("id"));
					li.set(LabItem.PRIO, prio);
				}
				reload();
			}
		};
		
	}
	
	private void doMove(Object[] objects, String group){
		for (Object o : objects) {
			if (o instanceof Item) {
				Item item = (Item) o;
				String id = item.get("id");
				LabItem li = LabItem.load(id);
				if (li != null && li.isValid()) {
					li.set(LabItem.GROUP, group);
				}
			}
		}
		reload();
	}
	
	private void reload(){
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run(){
				try {
					getLRS().reload();
				} catch (ElexisException eex) {
					log.error("could not reload LabItems " + eex.getMessage(), eex);
				}
			}
			
		});
		
	}
	
	private void doCombine(Object[] objects){
		Item target = (Item) objects[0];
		LabItem targetLI = LabItem.load(target.get("id"));
		log.info("combining " + objects.length + " LabItems into " + targetLI.getLabel());
		if (targetLI != null && targetLI.isValid()) {
			for (int i = 1; i < objects.length; i++) {
				Item it = (Item) objects[i];
				LabItem li = LabItem.load(it.get("id"));
				log.info("processing " + li.getLabel());
				Query<LabResult> qbe = new Query<LabResult>(LabResult.class);
				qbe.add(LabResult.ITEM_ID, Query.EQUALS, li.getId());
				List<LabResult> lrs = qbe.execute();
				if (SWTHelper.askYesNo("Laborresultate zusammenführen",
					"Wirklich " + lrs.size() + " Resultate aus " + li.getLabel() + " nach "
						+ targetLI.getLabel() + " verschieben?")) {
					for (LabResult lr : lrs) {
						log.debug("converting " + lr.getLabel());
						lr.set(LabResult.ITEM_ID, targetLI.getId());
					}
					
					li.delete();
				}
			}
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
				IStructuredSelection sel = (IStructuredSelection) tvFull.getSelection();
				organizeItemAction.setEnabled(false);
				toGroupAction.setEnabled(false);
				combineAction.setEnabled(false);
				sortAction.setEnabled(false);
				if (sel.size() == 1) {
					if (sel.getFirstElement() instanceof String) {
						sortAction.setEnabled(true);
					} else {
						organizeItemAction.setEnabled(true);
						toGroupAction.setEnabled(true);
					}
				} else if (sel.size() == 2) {
					if (!hasGroup(sel.toArray())) {
						toGroupAction.setEnabled(true);
						combineAction.setEnabled(true);
					}
				} else {
					Object[] objects = sel.toArray();
					Item cmb = null;
					boolean tgPossible = true;
					boolean cmPossible = true;
					if (hasGroup(objects)) {
						tgPossible = false;
						cmPossible = false;
					} else {
						for (Object o : objects) {
							Item item = (Item) o;
							if (cmb == null || cmb.isEqual(item)) {
								cmb = item;
							} else {
								cmPossible = false;
								break;
							}
						}
					}
					if (tgPossible) {
						toGroupAction.setEnabled(true);
					}
					if (cmPossible) {
						combineAction.setEnabled(true);
					}
				}
			}
		});
		tvFull.getTree().setMenu(createContextMenu(tvFull.getTree()));
		return tree;
	}
	
	private Boolean hasGroup(Object[] objects){
		for (Object o : objects) {
			if (o instanceof String) {
				return true;
			}
		}
		return false;
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
				mgr.add(sortAction);
				mgr.add(organizeItemAction);
				mgr.add(toGroupAction);
				mgr.add(combineAction);
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

	public void dispose(){
		controller.getLRS().removeObserver(this);
	}

	public String getState(){
		return "";
	}

	public void setState(String string){
		// TODO Auto-generated method stub
		
	}
}

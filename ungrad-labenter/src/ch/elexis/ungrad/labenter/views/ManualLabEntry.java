/*******************************************************************************
 * Copyright (c) 2018-2024 by G. Weirich
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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.DateSelectorDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labenter.views.LabEntryTable.Element;
import ch.rgw.tools.TimeTool;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view shows data obtained
 * from the model. The sample creates a dummy model on the fly, but a real implementation would
 * connect to the model available either in this or another plug-in (e.g. the workspace). The view
 * is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be presented in the view. Each
 * view can present the same model objects using different labels and icons, if needed.
 * Alternatively, a single label provider can be shared between views in order to ensure that
 * objects of the same type are presented in the same way everywhere.
 * <p>
 */

public class ManualLabEntry extends ViewPart implements IActivationListener {
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ch.elexis.ungrad.labenter.views.manualentries";
	
	private TableViewer viewer;
	private IAction changeDateAction;
	private IAction sendValuesAction;
	private IAction clearAction;
	private TimeTool actDate = new TimeTool();
	private FormToolkit tk;
	private Form form;
	private Patient pat;
	private LabEntryTable let;
	private IContextService ctx=ContextServiceHolder.get();
	
	private final ElexisUiEventListenerImpl eeli_pat =
		new ElexisUiEventListenerImpl(Patient.class, ElexisEvent.EVENT_SELECTED) {
			
			@Override
			public void runInUi(ElexisEvent ev){
				setLabel();
			}
			
		};
	
	/**
	 * The constructor.
	 */
	public ManualLabEntry(){}
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent){
		setTitleImage(Images.IMG_EDIT.getImage());
		tk = UiDesk.getToolkit();
		form = tk.createForm(parent);
		form.getBody().setLayout(new GridLayout(1, true));
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		setLabel();
		let = new LabEntryTable(form.getBody());
		viewer = let.viewer;
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		GlobalEventDispatcher.addActivationListener(this, this);
		
	}
	
	private void setLabel(){
		pat=(Patient) ctx.getActivePatient().orElseGet(null);
		// pat = ElexisEventDispatcher.getSelectedPatient();
		String lab = pat == null ? "Kein Patient gewählt" : pat.getLabel();
		form.setText("Labor von " + lab + ", vom " + actDate.toString(TimeTool.DATE_GER));
	}
	
	private void hookContextMenu(){
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				ManualLabEntry.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	private void contributeToActionBars(){
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void fillLocalPullDown(IMenuManager manager){
		manager.add(changeDateAction);
		manager.add(sendValuesAction);
		manager.add(clearAction);
	}
	
	private void fillContextMenu(IMenuManager manager){
		/*
		 * manager.add(action1); manager.add(action2); // Other plug-ins can contribute
		 * there actions here manager.add(new
		 * Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		 */
	}
	
	private void fillLocalToolBar(IToolBarManager manager){
		
		manager.add(changeDateAction);
		manager.add(sendValuesAction);
		manager.add(clearAction);
		
	}
	
	public void visible(final boolean mode){
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
		}
	}
	
	private void clearFields(){
		for (Element el : let.elements) {
			el.value = "";
		}
		viewer.setInput(let.elements);
	}
	
	private void makeActions(){
		changeDateAction = new Action() {
			{
				setText("Anderes Datum");
				setToolTipText("Datum für diese Laborwerte eingeben");
				setImageDescriptor(Images.IMG_CALENDAR.getImageDescriptor());
			}
			
			public void run(){
				DateSelectorDialog dsl = new DateSelectorDialog(getViewSite().getShell());
				if (dsl.open() == Dialog.OK) {
					actDate = dsl.getSelectedDate();
					setLabel();
				}
			}
		};
		
		sendValuesAction = new Action() {
			{
				setText("Absenden");
				setToolTipText("Diese Werte speichern");
				setImageDescriptor(Images.IMG_EDIT_DONE.getImageDescriptor());
			}
			
			public void run(){
				if (pat == null) {
					showMessage("Es ist kein Patient ausgewählt");
					
				} else {
					if (SWTHelper.askYesNo("Daten eintragen", "Wirklich die Daten für\n\n"
						+ pat.getLabel() + ", Datum "+actDate.toString(TimeTool.DATE_GER)+"\n\neintragen?")) {
						BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
							public void run(){
								for (Element el : let.elements) {
									if (!el.value.isEmpty()) {
										new LabResult(pat, actDate, el.item, el.value, "");
									}
								}
								showMessage("ok");
								clearFields();
							}
						});
					}
				}
			}
		};
		clearAction = new Action() {
			{
				setText("Alles löschen");
				setToolTipText("Formulareingaben leeren");
				setImageDescriptor(Images.IMG_CLEAR.getImageDescriptor());
			}
			
			public void run(){
				clearFields();
			}
		};
		
	}
	
	private void hookDoubleClickAction(){
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event){
				// doubleClickAction.run();
			}
		});
	}
	
	private void showMessage(String message){
		MessageDialog.openInformation(viewer.getControl().getShell(), "Laboreingabe", message);
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus(){
		viewer.getControl().setFocus();
	}
	
	@Override
	public void activation(boolean mode){
		// TODO Auto-generated method stub
		
	}
	
}

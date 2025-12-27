/*******************************************************************************
 * Copyright (c) 2018-2025 by G. Weirich
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

import org.eclipse.e4.core.di.annotations.Optional;
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

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.DateSelectorDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.IRefreshable;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labenter.views.LabEntryTable.Element;
import ch.rgw.tools.TimeTool;
import jakarta.inject.Inject;

public class ManualLabEntry extends ViewPart implements IRefreshable {

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
	private IPatient pat;
	private LabEntryTable let;
	private IContextService ctx = ContextServiceHolder.get();
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	@Inject
	void activePatient(@Optional IPatient patient) {
		CoreUiUtil.runAsyncIfActive(() -> {
			setLabel();
		}, form);
	}

	/**
	 * The constructor.
	 */
	public ManualLabEntry() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
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
		getSite().getPage().addPartListener(udpateOnVisible);

	}

	@Override
	public void refresh() {
		pat = (ctx.getActivePatient().orElse(null));
	}

	private void setLabel() {
		pat = (IPatient) ctx.getActivePatient().orElseGet(() -> null);
		// pat = ElexisEventDispatcher.getSelectedPatient();
		String lab = pat == null ? ch.elexis.ungrad.labenter.Messages.ManualLabEntry_NoPatientSelected : pat.getLabel();
		form.setText(java.text.MessageFormat.format(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_LabOf, lab) + ", " + 
				java.text.MessageFormat.format(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_From, actDate.toString(TimeTool.DATE_GER)));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ManualLabEntry.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(changeDateAction);
		manager.add(sendValuesAction);
		manager.add(clearAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		/*
		 * manager.add(action1); manager.add(action2); // Other plug-ins can contribute
		 * there actions here manager.add(new
		 * Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		 */
	}

	private void fillLocalToolBar(IToolBarManager manager) {

		manager.add(changeDateAction);
		manager.add(sendValuesAction);
		manager.add(clearAction);

	}

	private void clearFields() {
		for (Element el : let.elements) {
			el.value = "";
		}
		viewer.setInput(let.elements);
	}

	private void makeActions() {
		changeDateAction = new Action() {
			{
				setText(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Action_ChangeDate_Title);
				setToolTipText(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Action_ChangeDate_Tooltip);
				setImageDescriptor(Images.IMG_CALENDAR.getImageDescriptor());
			}

			public void run() {
				DateSelectorDialog dsl = new DateSelectorDialog(getViewSite().getShell());
				if (dsl.open() == Dialog.OK) {
					actDate = dsl.getSelectedDate();
					setLabel();
				}
			}
		};

		sendValuesAction = new Action() {
			{
				setText(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Action_Send_Title);
				setToolTipText(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Action_Send_Tooltip);
				setImageDescriptor(Images.IMG_EDIT_DONE.getImageDescriptor());
			}

			public void run() {
				if (pat == null) {
					showMessage(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_NoPatientSelected);

				} else {
					if (SWTHelper.askYesNo(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Dialog_EnterData_Title, 
							java.text.MessageFormat.format(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Dialog_EnterData_Message,
									pat.getLabel(), actDate.toString(TimeTool.DATE_GER)))) {
						BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
							public void run() {
								for (Element el : let.elements) {
									if (!el.value.isEmpty()) {
										new LabResult(Patient.load(pat.getId()), actDate, el.item, el.value, "");
									}
								}
								showMessage(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Dialog_Success);
								clearFields();
							}
						});
					}
				}
			}
		};
		clearAction = new Action() {
			{
				setText(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Action_Clear_Title);
				setToolTipText(ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Action_Clear_Tooltip);
				setImageDescriptor(Images.IMG_CLEAR.getImageDescriptor());
			}

			public void run() {
				clearFields();
			}
		};

	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), ch.elexis.ungrad.labenter.Messages.ManualLabEntry_Title, message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}

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

import static ch.rgw.elexis.docmgr_lucinda.Preferences.COLUMN_WIDTHS;
import static ch.rgw.elexis.docmgr_lucinda.Preferences.INCLUDE_KONS;
import static ch.rgw.elexis.docmgr_lucinda.Preferences.INCLUDE_OMNI;
import static ch.rgw.elexis.docmgr_lucinda.Preferences.RESTRICT_CURRENT;
import static ch.rgw.elexis.docmgr_lucinda.Preferences.SHOW_CONS;
import static ch.rgw.elexis.docmgr_lucinda.Preferences.SHOW_INBOX;
import static ch.rgw.elexis.docmgr_lucinda.Preferences.SHOW_OMNIVORE;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Patient;
import ch.rgw.elexis.docmgr_lucinda.Activator;
import ch.rgw.elexis.docmgr_lucinda.Preferences;
import ch.rgw.elexis.docmgr_lucinda.controller.Controller;

public class GlobalView extends ViewPart implements IActivationListener {

	private Controller controller;
	private Action doubleClickAction, filterCurrentPatAction, showInboxAction, showConsAction, showOmnivoreAction;
	private RestrictedAction indexOmnivoreAction, indexKonsAction;

	private final ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
			ElexisEvent.EVENT_SELECTED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			controller.changePatient((Patient) ev.getObject());
		}
	

	};

	public GlobalView() {
		controller = new Controller();
	}

	@Override
	public void createPartControl(Composite parent) {
		/*
		 * If the view is set to show nothing at all (which is the case e.g. on first launch), set it to show all results.
		 */
		if((is(SHOW_CONS)||is(SHOW_INBOX)||is(SHOW_OMNIVORE))==false){
			save(SHOW_CONS,true);
			save(SHOW_OMNIVORE,true);
			save(SHOW_INBOX,true);
		}

		makeActions();
		controller.createView(parent);
		contributeToActionBars();
		String colWidths=load(COLUMN_WIDTHS);
		controller.setColumnWidths(colWidths);
		GlobalEventDispatcher.addActivationListener(this, this);
	}

	public void visible(final boolean mode) {
		controller.reload();
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
			save(COLUMN_WIDTHS,controller.getColumnWidths());
		}
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(filterCurrentPatAction);
		manager.add(showInboxAction);
		manager.add(showConsAction);
		manager.add(showOmnivoreAction);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(indexOmnivoreAction);
		manager.add(indexKonsAction);
		manager.add(filterCurrentPatAction);

	}

	private void makeActions() {
		indexOmnivoreAction = new RestrictedAction(AccessControlDefaults.DOCUMENT_CREATE, Messages.GlobalView_omnivoreImport_Name,
				Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_omnivoreImport_tooltip);
				setImageDescriptor(Images.IMG_DATABASE.getImageDescriptor());
			}

			@Override
			public void doRun() {
				Activator.getDefault().syncOmnivore(this.isChecked());
				save(INCLUDE_OMNI, isChecked());
			}
		};
		indexOmnivoreAction.setChecked(is(INCLUDE_OMNI));

		indexKonsAction = new RestrictedAction(AccessControlDefaults.DOCUMENT_CREATE, Messages.GlobalView_synckons_Name,
				Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_synckons_tooltip);
				setImageDescriptor(Images.IMG_GEAR.getImageDescriptor());
			}

			@Override
			public void doRun() {
				Activator.getDefault().syncKons(this.isChecked());
				save(INCLUDE_KONS, isChecked());
			}
		};
		indexKonsAction.setChecked(is(INCLUDE_KONS));

		filterCurrentPatAction = new Action(Messages.GlobalView_actPatient_name, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_actPatient_tooltip);
				setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
			}

			@Override
			public void run() {
				controller.restrictToCurrentPatient(isChecked());
				save(RESTRICT_CURRENT, isChecked());
			}
		};
		filterCurrentPatAction.setChecked(is(RESTRICT_CURRENT));
		/*
		 * Show results from consultation texts
		 */
		showConsAction = new Action(Messages.GlobalView_filterKons_name, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_filterKons_tooltip);
			}

			@Override
			public void run() {
				controller.toggleDoctypeFilter(isChecked(), Preferences.KONSULTATION_NAME);
				save(SHOW_CONS,isChecked());
			}
		};
		showConsAction.setChecked(is(SHOW_CONS));;
		
		/*
		 * Show results from Omnivore
		 */
		showOmnivoreAction = new Action(Messages.GlobalView_filterOmni_name, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_filterOmni_tooltip);
			}

			@Override
			public void run() {
				controller.toggleDoctypeFilter(isChecked(), Preferences.OMNIVORE_NAME);
				save(SHOW_OMNIVORE,isChecked());
			}

		};
		showOmnivoreAction.setChecked(is(SHOW_OMNIVORE));
		/*
		 * Show results from Lucinda Inbox
		 */
		showInboxAction = new Action(Preferences.INBOX_NAME, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_filterInbox_name);
			}

			@Override
			public void run() {
				controller.toggleDoctypeFilter(isChecked(), Preferences.INBOX_NAME);
				save(SHOW_INBOX,isChecked());
			}

		};
		showInboxAction.setChecked(is(SHOW_INBOX));
	}

	@Override
	public void activation(boolean mode) {
	}

	private void save(String name, boolean value) {
		save(name, Boolean.toString(value));
	}

	private void save(String name, String value) {
		Preferences.set(name, value);
	}

	private String load(String name) {
		return Preferences.get(name, Messages.GlobalView_11);
	}

	private boolean is(String name) {
		return Boolean.parseBoolean(Preferences.get(name, Boolean.toString(false)));
	}

}

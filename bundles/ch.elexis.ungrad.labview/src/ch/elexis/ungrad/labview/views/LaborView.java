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
package ch.elexis.ungrad.labview.views;

import java.net.URL;

import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.Importer;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.IRefreshable;
import ch.elexis.ungrad.labview.controller.Controller;

public class LaborView extends ViewPart implements IRefreshable {
	Controller controller = new Controller(this);
	Log log = Log.get("LaborView");
	CTabFolder cTabFolder;
	private IContextService ctx=ContextServiceHolder.get();
	private Action exportHtmlAction, viewInBrowserAction, importAction, removeEmptyItemsAction;
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	
	@Inject
	void activePatient(@Optional IPatient patient) {
		CoreUiUtil.runAsyncIfActive(() -> {
			if (patient != null) {
				try {
					controller.setPatient(patient);
				} catch (ElexisException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, cTabFolder);
	}

	/*
	 * private final ElexisUiEventListenerImpl eeli_pat = new
	 * ElexisUiEventListenerImpl(Patient.class, ElexisEvent.EVENT_SELECTED) {
	 * 
	 * @Override public void runInUi(ElexisEvent ev){ try {
	 * controller.setPatient((Patient) ev.getObject()); } catch (ElexisException e)
	 * { log.log(e, "error loading patient data", Log.ERRORS); } }
	 * 
	 * };
	 */
	@Override
	public void createPartControl(Composite parent) {
		cTabFolder = new CTabFolder(parent, SWT.BOTTOM);
		cTabFolder.setLayoutData(SWTHelper.getFillGridData());
		CTabItem ctSmart = new CTabItem(cTabFolder, SWT.NONE);
		ctSmart.setText("Kompakt");
		ctSmart.setControl(controller.createSmartControl(cTabFolder));
		CTabItem ctSummary = new CTabItem(cTabFolder, SWT.NONE);
		ctSummary.setText("Synopsis");
		CTabItem ctFull = new CTabItem(cTabFolder, SWT.NONE);
		ctFull.setText("Voll");
		Control ctlSummary = controller.createSummaryControl(cTabFolder);
		// ctlSummary.setLayoutData(SWTHelper.getFillGridData());
		ctSummary.setControl(ctlSummary);
		Control ctlFull = controller.createFullControl(cTabFolder);
		ctFull.setControl(ctlFull);
		cTabFolder.setSelection(ctSmart);
		makeActions();
		contributeToActionBars();
		// GlobalEventDispatcher.addActivationListener(this, this);
		getSite().getPage().addPartListener(udpateOnVisible);
		controller.loadState();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(exportHtmlAction);
		manager.add(viewInBrowserAction);
		manager.add(importAction);
		// manager.add(removeEmptyItemsAction);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(exportHtmlAction);
		manager.add(removeEmptyItemsAction);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void visible(final boolean mode) {

	}

	/*
	@Override
	public void activation(boolean mode) {
		if (mode == true) {
			try {
				controller.setPatient(ctx.getActivePatient().orElse(null));
			} catch (ElexisException e) {
				log.log(e, "error loading patient data", Log.ERRORS);
			}
		} else {
			controller.saveState();
		}

	}
	*/

	@Override
	public void refresh() {
		try {
			controller.setPatient(ctx.getActivePatient().orElse(null));
		} catch (ElexisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		controller.dispose();
	}

	private void makeActions() {
		exportHtmlAction = new Action() {
			{
				setImageDescriptor(Images.IMG_WEB.getImageDescriptor());
				setToolTipText("Laborblatt exportieren");
			}

			@Override
			public void run() {
				controller.getExporter().createHTML(LaborView.this.getSite().getShell());
			}

		};
		viewInBrowserAction = new Action() {
			{
				setImageDescriptor(Images.IMG_EYE_WO_SHADOW.getImageDescriptor());
				setToolTipText("In Browser ansehen");
			}

			@Override
			public void run() {
				controller.getExporter().runInBrowser();
			}
		};
		importAction = new Action("Labordaten import") {
			{
				setImageDescriptor(Images.IMG_IMPORT.getImageDescriptor());
				setToolTipText("Resultate importieren");
			}

			@Override
			public void run() {
				Importer imp = new Importer(getViewSite().getShell(), ExtensionPointConstantsUi.LABORDATENIMPORT); // $NON-NLS-1$
				imp.create();
				imp.setMessage("Bitte Datenquelle auswählen");
				imp.getShell().setText("Labordaten import");
				imp.setTitle("Labor Auswahl");
				imp.open();
			}
		};
		removeEmptyItemsAction = new Action("Aufräumen") {
			{
				setToolTipText("Ungebrauchte Items und Gruppen löschen");
				Bundle bundle = FrameworkUtil.getBundle(this.getClass());
				URL url = FileLocator.find(bundle, new Path("icons/edit-clear24.png"), null);
				setImageDescriptor(ImageDescriptor.createFromURL(url));
			}

			@Override
			public void run() {
				if (SWTHelper.askYesNo("Laboritems aufräumen",
						"Alle Laboritems entfernen, für die keine Resultate existieren. (Das kann sehr lange dauern)")) {
					controller.purgeLabItems();
				}

			}
		};
	}
}

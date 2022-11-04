/*******************************************************************************
 * Copyright (c) 2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.forms.ui;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.Mailer;
import ch.elexis.ungrad.forms.model.Controller;
import ch.elexis.ungrad.forms.model.PreferenceConstants;
import ch.elexis.ungrad.forms.model.Template;
import ch.elexis.ungrad.pdf.Medform;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * @author gerry
 *
 */
public class View extends ViewPart implements IActivationListener {
	private Controller controller;
	private Action createNewAction, showListAction, showDetailAction, printAction, mailAction;
	private DocumentList docList;
	private DetailDisplay detail;
	private Composite container;
	private StackLayout stack;
	private Template currentTemplate;

	private final ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
			ElexisEvent.EVENT_SELECTED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			// controller.changePatient((Patient) ev.getObject());
			docList.setPatient((Patient) ev.getObject());
			stack.topControl = docList;
			detail.clear();
			container.layout();
		}

	};

	public View() {
		controller = new Controller();
		stack = new StackLayout();
	}

	@Override
	public void createPartControl(Composite parent) {
		visible(true);
		container = new Composite(parent, SWT.NONE);
		container.setLayout(stack);
		docList = new DocumentList(container, controller);
		detail = new DetailDisplay(container, controller);
		makeActions();
		contributeToActionBars();
		GlobalEventDispatcher.addActivationListener(this, this);
		stack.topControl = docList;
		container.layout();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		IMenuManager menu = bars.getMenuManager();
		IToolBarManager toolbar = bars.getToolBarManager();
		toolbar.add(createNewAction);
		toolbar.add(showListAction);
		toolbar.add(showDetailAction);
		toolbar.add(printAction);
		toolbar.add(mailAction);
		printAction.setEnabled(false);
		showDetailAction.setEnabled(false);
		docList.addSelectionListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = event.getStructuredSelection();
				boolean bHasSelection = !sel.isEmpty();
				printAction.setEnabled(bHasSelection);
				showDetailAction.setEnabled(bHasSelection);

			}
		});
		docList.addDoubleclickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				showDetailAction.run();
			}
		});
		// menu.add();

	}

	private void makeActions() {
		createNewAction = new Action("Laden") {
			{
				setToolTipText("Ein neues Dokument erstellen");
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setText("Neu");
			}

			@Override
			public void run() {
				SelectTemplateDialog std = new SelectTemplateDialog(getViewSite().getShell());
				if (std.open() == Dialog.OK) {
					File templateFile = std.result;
					try {
						if (templateFile.getName().endsWith("pdf")) {
							Patient currentPat = ElexisEventDispatcher.getSelectedPatient();
							File outDir = controller.getOutputDirFor(currentPat);
							if (!outDir.exists()) {
								if (!outDir.mkdirs()) {
									throw new Error("Can't create output dir " + outDir.getAbsolutePath());
								}
							}
							String basename = "A_" + new TimeTool().toString(TimeTool.DATE_ISO) + "_"
									+ templateFile.getName();
							File outFile = new File(outDir, basename);
							Medform medform = new Medform(templateFile.getAbsolutePath());
							medform.create(outFile.getAbsolutePath(), currentPat);
							detail.asyncRunViewer(outFile.getAbsolutePath());
						} else {
							Kontakt adressat = null;
							String html = FileTool.readTextFile(templateFile);
							if (templateFile.getName().endsWith("pug")) {
								String dir = templateFile.getParent();
								html = controller.convertPug(html, dir);
							}
							if (html.contains("[Adressat")) {
								KontaktSelektor ksd = new KontaktSelektor(getSite().getShell(), Kontakt.class,
										"Adressat", "Bitte Adressat auswählen",
										new String[] { "Bezeichnung1", "Bezeichnung2" });
								if (ksd.open() != Dialog.OK) {
									return;
								} else {
									adressat = (Kontakt) ksd.getSelection();
								}
							}
							currentTemplate = new Template(html, adressat);
							detail.show(currentTemplate);
							printAction.setEnabled(true);
							mailAction.setEnabled(true);
							stack.topControl = detail;
							container.layout();
						}

					} catch (Exception e) {
						ExHandler.handle(e);
						SWTHelper.showError("Fehler bei Ausgabe", e.getMessage());
					}
				}
			}
		};
		showListAction = new Action("Dokumentenliste") {
			{
				setText("Dokumente");
				setImageDescriptor(Images.IMG_DOCUMENT_STACK.getImageDescriptor());
				setToolTipText("Zeige Liste der Dokumente");
			}

			@Override
			public void run() {
				stack.topControl = docList;
				if (docList.getSelection() == null) {
					printAction.setEnabled(false);
					mailAction.setEnabled(false);
				}
				container.layout();
			}
		};
		showDetailAction = new Action("Formular") {
			{
				setText("Ausfüllen");
				setImageDescriptor(Images.IMG_DOCUMENT_PDF.getImageDescriptor());
				setToolTipText("Zeige aktuelles Formular");
			}

			@Override
			public void run() {
				stack.topControl = detail;
				File dir = controller.getOutputDirFor(null);
				File document = new File(dir, docList.getSelection() + ".html");
				if (document.exists()) {
					try {
						String html = FileTool.readTextFile(document);
						currentTemplate = new Template(html, null);
						currentTemplate.setFilename(document.getAbsolutePath());
						detail.show(currentTemplate);
					} catch (Exception e) {
						SWTHelper.showError("Fehler bei Verarbeitung", e.getMessage());
					}
				}
				container.layout();
			}
		};
		printAction = new Action("Ausgabe") {
			{
				setText("Ausgeben");
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText("Aktuelles Formular erstellen und ausgeben");
			}

			@Override
			public void run() {
				if (stack.topControl.equals(detail)) {
					detail.output();
				} else if (stack.topControl.equals(docList)) {
					docList.output();
				}
			}
		};
		mailAction = new Action("Per Mail senden") {
			{
				setText("Senden");
				setImageDescriptor(Images.IMG_MAIL_SEND.getImageDescriptor());
				setToolTipText("Dokument als PDF per Mail versenden");
			}

			@Override
			public void run() {
				detail.sendMail();
			}

		};
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void activation(boolean mode) {

	}

	@Override
	public void visible(boolean mode) {
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
		}

	}

}

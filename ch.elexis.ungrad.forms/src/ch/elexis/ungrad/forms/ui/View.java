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
import java.io.IOException;
import java.util.List;
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
import ch.elexis.data.Query;
import ch.elexis.ungrad.forms.Activator;
import ch.elexis.ungrad.forms.model.Controller;
import ch.elexis.ungrad.forms.model.Template;
import ch.elexis.ungrad.pdf.MappedForm;
import ch.elexis.ungrad.pdf.Medform;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * Main View of the Elexis-Forms Plugin
 * 
 * @author gerry
 *
 */
public class View extends ViewPart implements IActivationListener {
	private Controller controller;
	private Action createNewAction, showListAction, showDetailAction, printAction, mailAction, deleteAction;
	private DocumentList docList;
	private DetailDisplay detail;
	private Composite container;
	private StackLayout stack;
	private Template currentTemplate;

	private final ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
			ElexisEvent.EVENT_SELECTED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			setPatient((Patient) ev.getObject());
		}

	};

	public View() {
		controller = Activator.getController();
		stack = new StackLayout();
	}

	void setPatient(Patient pat) {
		// controller.changePatient((Patient) ev.getObject());
		docList.setPatient(pat);
		stack.topControl = docList;
		detail.clear();
		container.layout();
	}

	@Override
	public void createPartControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(stack);
		docList = new DocumentList(container, controller);
		detail = new DetailDisplay(container, controller);
		makeActions();
		contributeToActionBars();
		stack.topControl = docList;
		container.layout();
		GlobalEventDispatcher.addActivationListener(this, this);

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
		mailAction.setEnabled(false);
		docList.addSelectionListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = event.getStructuredSelection();
				boolean bHasSelection = !sel.isEmpty();
				printAction.setEnabled(bHasSelection);
				showDetailAction.setEnabled(bHasSelection);
				mailAction.setEnabled(true);
			}
		});
		docList.addDoubleclickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				showDetailAction.run();
			}
		});
		menu.add(deleteAction);

	}

	/**
	 * If the Template is a PDF: - Check for medForms-signature and if so, fill
	 * medForms standardFiels and store in output dir. - If not, check for
	 * corrseponding .map-file. If found, fill fields from .map and store n output
	 * dir. - If not, just open file.
	 * 
	 * @param templateFile the template to check
	 * @return full path of the resulting file in the output dir.
	 */
	private String fillPdf(File templateFile) throws Error, Exception, IOException {
		Patient currentPat = ElexisEventDispatcher.getSelectedPatient();
		File outDir = controller.getOutputDirFor(currentPat);
		if (!outDir.exists()) {
			if (!outDir.mkdirs()) {
				throw new Error("Can't create output dir " + outDir.getAbsolutePath());
			}
		}
		String basename = "A_" + new TimeTool().toString(TimeTool.DATE_ISO) + "_" + templateFile.getName();
		File outFile = new File(outDir, basename);
		Medform medform = new Medform(templateFile.getAbsolutePath());
		Kontakt kRecipient = null;
		if (medform.isMedform()) {
			medform.create(outFile.getAbsolutePath(), currentPat);
			String recipient = medform.getFieldValue("receiverMail");
			Query<Kontakt> qbe = new Query<Kontakt>(Kontakt.class, Kontakt.FLD_E_MAIL, recipient);
			List<Kontakt> found = qbe.execute();
			if (found.size() > 0) {
				kRecipient = found.get(0);
			}
		} else {
			File mappingFile = new File(templateFile.getParent(),
					FileTool.getNakedFilename(templateFile.getName()) + ".map");
			if (mappingFile.exists()) {
				String raw = FileTool.readTextFile(mappingFile);
				MappedForm mapped = new MappedForm(templateFile.getAbsolutePath());
				mapped.create(outFile.getAbsolutePath(), raw, currentPat);
			}
		}
		controller.createLinksWithElexis(outFile.getAbsolutePath(), kRecipient);
		return outFile.getAbsolutePath();
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
							String outFile = fillPdf(templateFile);
							detail.asyncRunViewer(outFile);

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
						docList.setPatient(ElexisEventDispatcher.getSelectedPatient());

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
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
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
				} else {
					document = new File(dir, docList.getSelection() + ".pdf");
					if (document.exists()) {
						detail.asyncRunViewer(document.getAbsolutePath());
					}
				}
				printAction.setEnabled(true);
				mailAction.setEnabled(true);
				container.layout();
			}
		};
		printAction = new Action("Ausgabe") {
			{
				setText("Ausgeben");
				setImageDescriptor(Images.IMG_DOCUMENT_PDF.getImageDescriptor());
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
				if (stack.topControl.equals(detail)) {
					detail.sendMail();
				} else if (stack.topControl.equals(docList)) {
					docList.sendMail();
				}
			}

		};
		deleteAction = new Action("Dokument löschen") {
			{
				setText("Löschen");
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText("Dokument unwiderruflich löschen");
			}

			@Override
			public void run() {
				try {
					String sel = docList.getSelection();
					if (SWTHelper.askYesNo("Bitte bestätigen", sel + " Wirklich unwiderruflich löschen?")) {
						Patient pat = ElexisEventDispatcher.getSelectedPatient();
						controller.delete(sel, pat);
						docList.setPatient(pat);
					}
				} catch (Exception e) {
					ExHandler.handle(e);
					SWTHelper.showError("Fehler beim Löschen", e.getMessage());
				}
			}
		};
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void activation(boolean mode) {
		if (!mode) {
			try {
				if (stack.topControl.equals(detail)) {
					detail.saveHtml();
				}
			} catch (Exception ex) {
				SWTHelper.showError("Fehler beim Sichern", ex.getMessage());
			}
		}
	}

	@Override
	public void visible(boolean mode) {
		if (mode) {
			setPatient(ElexisEventDispatcher.getSelectedPatient());
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
		}

	}

}

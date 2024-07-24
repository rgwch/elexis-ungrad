/*******************************************************************************
 * Copyright (c) 2022-2024, G. Weirich and Elexis
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

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Query;
import ch.elexis.ungrad.forms.Activator;
import ch.elexis.ungrad.forms.model.Controller;
import ch.elexis.ungrad.forms.model.PreferenceConstants;
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
	private Action createNewAction, showListAction, showDetailAction, printAction, mailAction, deleteAction, signAction;
	private DocumentList docList;
	private DetailDisplay detail;
	private Composite container;
	private StackLayout stack;
	private Template currentTemplate;
	private boolean bHasSignature;
	private IPatient currentPatient;
	
	@Inject
	private IContextService ctx; // = ContextServiceHolder.get();

	@Inject
	void activePatient(@Optional IPatient patient) {
		CoreUiUtil.runAsyncIfActive(() -> {
			if (patient != null) {
				setPatient(patient);
			}
		}, container);
	}

	/*
	 * private final ElexisUiEventListenerImpl eeli_pat = new
	 * ElexisUiEventListenerImpl(Patient.class, ElexisEvent.EVENT_SELECTED) {
	 * 
	 * @Override public void runInUi(ElexisEvent ev) { setPatient((Patient)
	 * ev.getObject()); }
	 * 
	 * };
	 */

	public View() {
		controller = Activator.getController();
		stack = new StackLayout();
		String signature = CoreHub.localCfg.get(PreferenceConstants.SIGNATURE, null);
		if (signature != null && new File(signature).exists()) {
			bHasSignature = true;
		}
	}

	void setPatient(IPatient pat) {
		// controller.changePatient((Patient) ev.getObject());
		currentPatient = pat;
		docList.setPatient(pat);
		stack.topControl = docList;
		detail.clear();
		container.layout();
		setItemActions(docList.getSelection() != null);

	}

	private void setItemActions(boolean bMode) {
		printAction.setEnabled(bMode);
		mailAction.setEnabled(bMode);
		deleteAction.setEnabled(bMode);
		showDetailAction.setEnabled(bMode);
		signAction.setEnabled(bMode);
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
	
	@Override
	public void dispose() {
		GlobalEventDispatcher.removeActivationListener(this, this);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		IMenuManager menu = bars.getMenuManager();
		IToolBarManager toolbar = bars.getToolBarManager();
		toolbar.add(createNewAction);
		toolbar.add(new Separator());
		toolbar.add(showListAction);
		toolbar.add(showDetailAction);
		toolbar.add(new Separator());
		if (bHasSignature) {
			toolbar.add(signAction);
		}
		toolbar.add(printAction);
		toolbar.add(mailAction);
		menu.add(createNewAction);
		menu.add(printAction);
		menu.add(mailAction);
		menu.add(new Separator());
		menu.add(deleteAction);

		printAction.setEnabled(false);
		showDetailAction.setEnabled(false);
		mailAction.setEnabled(false);
		signAction.setEnabled(false);
		docList.addSelectionListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = event.getStructuredSelection();
				setItemActions(!sel.isEmpty());
			}
		});
		docList.addDoubleclickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				showDetailAction.run();
			}
		});

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
		File outDir = controller.getStorageController().getOutputDirFor(currentPatient.getId(), true);
		if (!outDir.exists()) {
			if (!outDir.mkdirs()) {
				throw new Error(Messages.View_CantCreateOutputDir + outDir.getAbsolutePath());
			}
		}
		String basename = "A_" + new TimeTool().toString(TimeTool.DATE_ISO) + "_" + templateFile.getName(); //$NON-NLS-1$ //$NON-NLS-2$
		File outFile = new File(outDir, basename);
		Medform medform = new Medform(templateFile.getAbsolutePath());
		Kontakt kRecipient = null;
		if (medform.isMedform()) {
			medform.create(outFile.getAbsolutePath(), currentPatient);
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
				mapped.create(outFile.getAbsolutePath(), raw);
			}
		}
		controller.createLinksWithElexis(outFile.getAbsolutePath(), kRecipient);
		return outFile.getAbsolutePath();
	}

	private void makeActions() {
		createNewAction = new Action(Messages.View_Create) {
			{
				setToolTipText(Messages.View_CreateNewDocument);
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setText(Messages.View_New);
			}

			@Override
			public void run() {
				SelectTemplateDialog std = new SelectTemplateDialog(getViewSite().getShell());
				if (std.open() == Dialog.OK) {
					File templateFile = std.result;
					try {
						if (templateFile.getName().endsWith("pdf")) {
							String outFile = fillPdf(templateFile);
							Program.launch(outFile);

						} else {
							Kontakt adressat = null;
							String html = FileTool.readTextFile(templateFile);
							if (templateFile.getName().endsWith("pug")) {
								String dir = templateFile.getParent();
								html = controller.convertPug(html, dir);
							}
							if (html.contains("[Adressat")) {
								KontaktSelektor ksd = new KontaktSelektor(getSite().getShell(), Kontakt.class,
										Messages.View_Receiver, Messages.View_PleaseSelectReceiver,
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
						docList.setPatient(currentPatient);

					} catch (Exception e) {
						ExHandler.handle(e);
						SWTHelper.showError(Messages.View_OutputError, e.getMessage());
					}
				}
			}

		};
		showListAction = new Action(Messages.View_Doclist) {
			{
				setText(Messages.View_Documents);
				setImageDescriptor(Images.IMG_DOCUMENT_STACK.getImageDescriptor());
				setToolTipText(Messages.View_ShowListOfDocuments);
			}

			@Override
			public void run() {
				stack.topControl = docList;
				setItemActions(docList.getSelection() != null);
				container.layout();
			}
		};
		showDetailAction = new Action(Messages.View_Form) {
			{
				setText(Messages.View_Completion);
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.View_ShowCurrentForm);
			}

			@Override
			public void run() {
				try {
					File dir = controller.getStorageController().getOutputDirFor(null, true);
					File document = new File(dir, docList.getSelection() + ".html");
					if (document.exists()) {
						String html = FileTool.readTextFile(document);
						currentTemplate = new Template(html, null);
						currentTemplate.setFilename(document.getAbsolutePath());
						stack.topControl = detail;
						detail.show(currentTemplate);
					} else {
						stack.topControl = docList;
						controller.showPDF(null, docList.getSelection());
					}
				} catch (Exception ex) {
					SWTHelper.showError(Messages.View_ErrorProcessing, ex.getMessage());

				}
				signAction.setEnabled(false);
				printAction.setEnabled(true);
				mailAction.setEnabled(true);
				container.layout();
			}
		};
		printAction = new Action(Messages.View_Output_Heading) {
			{
				setText(Messages.View_Output_Text);
				setImageDescriptor(Images.IMG_DOCUMENT_PDF.getImageDescriptor());
				setToolTipText(Messages.View_CreateAndOutput);
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
		mailAction = new Action(Messages.View_SendByMail) {
			{
				setText(Messages.View_Send_Header);
				setImageDescriptor(Images.IMG_MAIL_SEND.getImageDescriptor());
				setToolTipText(Messages.View_SendAsPDFByMail);
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
		signAction = new Action("Unterschreiben") {
			{
				setText("Dokument signieren");
				setImageDescriptor(Images.IMG_PERSON_OK.getImageDescriptor());
				setToolTipText("Das ausgewählte DOkument unterschreiben");
			}

			@Override
			public void run() {
				if (stack.topControl.equals(docList)) {
					docList.sign();
				}
			}
		};
		deleteAction = new Action(Messages.View_Delete_Header) {
			{
				setText(Messages.View_Delete_Text);
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.View_DeleteDocument);
			}

			@Override
			public void run() {
				try {
					String sel = docList.getSelection();
					if (SWTHelper.askYesNo(Messages.View_PleaseConfirm, sel + Messages.View_ReallyDelete)) {
						controller.delete(sel, currentPatient);
						docList.setPatient(currentPatient);
					}
				} catch (Exception e) {
					ExHandler.handle(e);
					SWTHelper.showError(Messages.View_ErrorDeleting, e.getMessage());
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
		if (mode==true) {
			setPatient(ctx.getActivePatient().orElse(null));
		} else {
			try {
				if (stack.topControl.equals(detail)) {
					detail.saveHtml();
				}
			} catch (Exception ex) {
				SWTHelper.showError(Messages.View_ErrorSaving, ex.getMessage());
			}
		}
	}

	@Override
	public void visible(boolean mode) {
		System.out.print("Visible "+mode);
		/*
		 * if (mode) { setPatient(ElexisEventDispatcher.getSelectedPatient());
		 * ElexisEventDispatcher.getInstance().addListeners(eeli_pat); } else {
		 * ElexisEventDispatcher.getInstance().removeListeners(eeli_pat); }
		 */
	}

}

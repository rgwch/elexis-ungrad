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

import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.common.ui.MailUI;
import ch.elexis.ungrad.forms.model.Controller;
import ch.elexis.ungrad.forms.model.PreferenceConstants;
import ch.elexis.ungrad.forms.model.Template;
import ch.elexis.ungrad.pdf.Medform;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public class DocumentList extends Composite {
	private TableViewer tv;
	private Controller controller;
	private IContextService contextService=ContextServiceHolder.get();
	
	public DocumentList(Composite parent, Controller controller) {
		super(parent, SWT.NONE);
		this.controller = controller;
		setLayoutData(SWTHelper.getFillGridData());
		setLayout(new GridLayout());
		tv = new TableViewer(this);
		tv.setContentProvider(controller);
		tv.setLabelProvider(controller);
		tv.setComparator(new ViewerComparator() {

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((String) e2).compareTo((String) e1);
			}

		});
		tv.setInput(contextService.getActivePatient());
		tv.getControl().setLayoutData(SWTHelper.getFillGridData());

	}

	public void addSelectionListener(ISelectionChangedListener listener) {
		tv.addSelectionChangedListener(listener);
	}

	public void addDoubleclickListener(IDoubleClickListener listener) {
		tv.addDoubleClickListener(listener);
	}

	/*
	 * Get the currently selected item or null if none is selected
	 */
	public String getSelection() {
		IStructuredSelection sel = tv.getStructuredSelection();
		if (sel.isEmpty()) {
			return null;
		} else {
			return (String) sel.getFirstElement();
		}
	}

	public void sign() {
		String sel = getSelection();
		if (sel != null) {
			File dir;
			try {
				dir = controller.getStorageController().getOutputDirFor(null, false);
				File file = new File(dir, sel + ".pdf");
				controller.signPDF(file);
				tv.refresh();
			} catch (Exception e) {
				SWTHelper.showError("Fehler beim Signieren", e.getMessage());
				ExHandler.handle(e);
			}
		}
	}

	public void sendMail() {
		IStructuredSelection sel = tv.getStructuredSelection();
		if (!sel.isEmpty()) {
			String selected = (String) sel.getFirstElement();
			try {
				File dir = controller.getStorageController().getOutputDirFor(null, false);
				if (dir.exists()) {
					File outfile = new File(dir, selected + ".pdf");
					File templateFile = new File(dir, selected + ".html");
					String subject = selected;
					String body = CoreHub.localCfg.get(PreferenceConstants.MAIL_BODY,
							Messages.DocumentList_MessageBodyDefault);
					String recipient = ""; //$NON-NLS-1$
					if (templateFile.exists()) {
						Template template = new Template(FileTool.readTextFile(templateFile), null);
						subject = template.getMailSubject();
						body = template.getMailBody();
						recipient = template.getMailRecipient();
					} else if (outfile.exists()) {
						Medform medform = new Medform(outfile.getAbsolutePath());
						recipient = medform.getFieldValue("receiverMail");
					}

					MailUI mailer = new MailUI(getShell());
					mailer.sendMail(subject, body, recipient, outfile.getAbsolutePath());
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
				SWTHelper.showError(Messages.DocumentList_PrintError, ex.getMessage());
			}

		}
	}

	public String output() {
		String selected = getSelection();
		if (selected != null) {
			try {
				controller.showPDF(null, selected);
			} catch (Exception ex) {
				ExHandler.handle(ex);
				SWTHelper.showError(Messages.DocumentList_CreateError, ex.getMessage());
			}
		}
		return null;
	}

	void setPatient(IPatient pat) {
		tv.setInput(pat);
	}
}

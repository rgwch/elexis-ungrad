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

import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;

import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.forms.model.Controller;
import ch.elexis.ungrad.forms.model.PreferenceConstants;
import ch.elexis.ungrad.forms.model.Template;
import ch.elexis.ungrad.pdf.Manager;
import ch.elexis.ungrad.pdf.Medform;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public class DocumentList extends Composite {
	private TableViewer tv;
	private Controller controller;

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
		tv.setInput(ElexisEventDispatcher.getSelectedPatient());
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

	public void sendMail() {
		IStructuredSelection sel = tv.getStructuredSelection();
		if (!sel.isEmpty()) {
			String selected = (String) sel.getFirstElement();
			try {
				File dir = controller.getOutputDirFor(null, false);
				if (dir.exists()) {
					File outfile = new File(dir, selected + ".pdf");
					File templateFile = new File(dir, selected + ".html");
					String subject = selected;
					String body = CoreHub.localCfg.get(PreferenceConstants.MAIL_BODY,
							"Siehe Anhang\nMit freundlichen Grüssen");
					String recipient = "";
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
				SWTHelper.showError("Fehler bei Ausdruck", ex.getMessage());
			}

		}
	}

	public String output() {
		IStructuredSelection sel = tv.getStructuredSelection();
		if (!sel.isEmpty()) {
			String selected = (String) sel.getFirstElement();
			try {
				controller.showPDF(null, selected);
			} catch (Exception ex) {
				ExHandler.handle(ex);
				SWTHelper.showError("Could not create or show file", ex.getMessage());
			}
		}
		return null;
	}

	void setPatient(Patient pat) {
		tv.setInput(pat);
	}
}

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
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.MailDialog;
import ch.elexis.ungrad.Mailer;
import ch.elexis.ungrad.forms.model.Controller;
import ch.elexis.ungrad.forms.model.PreferenceConstants;
import ch.elexis.ungrad.forms.model.Template;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class DetailDisplay extends Composite {

	ScrolledForm form;
	Composite inlay;
	Template template;
	Controller controller;

	public DetailDisplay(Composite parent, Controller controller) {
		super(parent, SWT.NONE);
		this.controller = controller;
		setLayoutData(SWTHelper.getFillGridData());
		setLayout(new FillLayout());
		FormToolkit tk = new FormToolkit(getDisplay());
		form = tk.createScrolledForm(this);
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		inlay = new Composite(body, SWT.NONE);
		inlay.setLayoutData(SWTHelper.getFillGridData());
		body.setBackground(new Color(getDisplay(), 100, 100, 100));
		inlay.setBackground(new Color(getDisplay(), 200, 200, 200));
		inlay.setLayout(new GridLayout());
	}

	void clear() {
		for (Control c : inlay.getChildren()) {
			c.dispose();
		}
		form.setText("");
	}

	void show(Template template) {
		this.template = template;
		form.setText(template.getTitle());
		clear();
		for (Entry<String, String> e : template.getInputs().entrySet()) {
			Label label = new Label(inlay, SWT.NONE);
			label.setText(e.getKey());
			label.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Text text = new Text(inlay, SWT.MULTI | SWT.BORDER);
			text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			String val = e.getValue();
			val = val.replace("<br />", "\n");
			text.setText(val);
			text.setData("input", e.getKey());
		}
		inlay.layout();
	}

	File saveHtml() throws Exception {
		for (Control c : inlay.getChildren()) {
			Object k = c.getData("input");
			if (k instanceof String) {
				String val = ((Text) c).getText().replaceAll("\n", "<br />");
				template.setInput((String) k, val);
			}
		}
		return controller.writeHTML(template);
	}

	public String output() {
		try {
			File htmlFile = saveHtml();
			String pdffile = controller.createPDF(htmlFile, template);
			asyncRunViewer(pdffile);
			return pdffile;
		} catch (Exception e) {
			SWTHelper.showError("Fehler bei Ausgabe", e.getMessage());
			ExHandler.handle(e);
			return null;
		}
	}

	public void sendMail() {
		MailUI mailer = new MailUI(getShell());
		String subject = template.getMailSubject();
		String body = template.getMailBody();
		String recipient = template.getMailRecipient();
		mailer.sendMail(subject, body, recipient, output());
	}

	void asyncRunViewer(String filepath) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					String ext = FileTool.getExtension(filepath); // $NON-NLS-1$

					Program proggie = Program.findProgram(ext);
					if (proggie != null) {
						proggie.execute(filepath);
					} else {
						if (Program.launch(filepath) == false) {
							Runtime.getRuntime().exec(filepath);
						}
					}

				} catch (Exception ex) {
					ExHandler.handle(ex);
					SWTHelper.showError("Could not create or show file", ex.getMessage());
				}
			}

		});

	}
}

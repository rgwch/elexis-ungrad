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
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.common.ui.MailUI;
import ch.elexis.ungrad.forms.model.Controller;
import ch.elexis.ungrad.forms.model.Template;
import ch.rgw.tools.ExHandler;

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
		inlay = new Composite(body, SWT.V_SCROLL);
		inlay.setLayoutData(SWTHelper.getFillGridData());
		body.setBackground(new Color(getDisplay(), 100, 100, 100));
		inlay.setBackground(new Color(getDisplay(), 200, 200, 200));
		inlay.setLayout(new GridLayout());

	}

	void clear() {
		for (Control c : inlay.getChildren()) {
			c.dispose();
		}
		form.setText(StringConstants.EMPTY);
	}

	void show(Template template) {
		this.template = template;

		clear();
		form.setText(template.getTitle());
		for (Entry<String, String> e : template.getInputs().entrySet()) {
			Label label = new Label(inlay, SWT.NONE);
			label.setText(e.getKey());
			label.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Text text = new Text(inlay, SWT.MULTI | SWT.BORDER);
			text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			String val = e.getValue();
			val = val.replace("<br />", StringConstants.LF); //$NON-NLS-1$
			text.setText(val);
			text.setData("input", e.getKey());
		} // inlay.pack();

		inlay.layout();
	}

	File saveHtml() throws Exception {
		for (Control c : inlay.getChildren()) {
			Object k = c.getData("input");
			if (k instanceof String) {
				String val = ((Text) c).getText().replaceAll(StringConstants.LF, "<br />"); //$NON-NLS-1$ //$NON-NLS-2$
				template.setInput((String) k, val);
			}
		}
		return controller.writeHTML(template);
	}

	public String output() {
		try {
			File htmlFile = saveHtml();
			String pdffile = controller.createPDF(htmlFile, template);
			controller.launchPDFViewerFor(pdffile);
	//		** Program.launch(pdffile);
			return pdffile;
		} catch (Exception e) {
			SWTHelper.showError(Messages.DetailDisplay_OutputError, e.getMessage());
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

}

/*******************************************************************************
 * Copyright (c) 2022-2023 by G. Weirich
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

package ch.elexis.ungrad.common.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.Mailer;
import ch.elexis.ungrad.PreferenceConstants;
import ch.elexis.ungrad.Resolver;
import ch.rgw.tools.ExHandler;

public class MailUI {
	Shell shell;

	public MailUI(Shell shell) {
		this.shell = shell;
	}

	/**
	 * Show a simple preview of a mail and allow attaching files from the file
	 * system
	 * 
	 * @param subject     subject line for the mail
	 * @param body        body of the mail
	 * @param recipient   receiver
	 * @param pdfFilePath first attachment
	 */
	public void sendMail(String subject, String body, String recipient, String pdfFilePath) {
		String user = CoreHub.localCfg.get(PreferenceConstants.SMTP_USER, "");
		String sender = CoreHub.localCfg.get(PreferenceConstants.MAIL_SENDER, user);
		String smtpserver = CoreHub.localCfg.get(PreferenceConstants.SMTP_HOST, "localhost");
		String smtppwd = CoreHub.localCfg.get(PreferenceConstants.SMTP_PWD, "doesntMatter");
		String smtpport = CoreHub.localCfg.get(PreferenceConstants.SMTP_PORT, "53");
		String[] attachments = new String[] { pdfFilePath };
		Resolver resolver = new Resolver();
		Mailer mailer = new Mailer(sender, smtpserver, smtppwd, smtpport);
		MailDialog mailDialog = new MailDialog(shell);
		mailDialog.sender = sender;
		mailDialog.mailTo = recipient;
		try {
			mailDialog.body = resolver.resolve(body);
			mailDialog.subject = resolver.resolve(subject);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			mailDialog.body = body;
			mailDialog.subject = subject;
		}
		mailDialog.attachments = attachments;
		if (mailDialog.open() == Dialog.OK) {
			sender = mailDialog.sender;
			subject = mailDialog.subject;
			body = mailDialog.body;
			recipient = mailDialog.mailTo;
			attachments = mailDialog.attachments;
			try {
				String sec = CoreHub.localCfg.get(PreferenceConstants.SMTP_SECURITY, "plain");
				if (sec.equals("plain")) {
					mailer.simpleMail(recipient, subject, body, attachments);
				} else if (sec.equals("tls")) {
					mailer.tlsMail(user, recipient, subject, body, attachments);
				} else if (sec.equals("ssl")) {
					mailer.sslMail(user, recipient, subject, body, attachments);
				}
			} catch (Exception e) {
				ExHandler.handle(e);
				SWTHelper.showError("Fehler beim Senden der Mail", e.getMessage());
			}
		}

	}
}

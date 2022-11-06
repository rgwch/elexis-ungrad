package ch.elexis.ungrad.forms.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.MailDialog;
import ch.elexis.ungrad.Mailer;
import ch.elexis.ungrad.forms.model.PreferenceConstants;
import ch.rgw.tools.ExHandler;

public class MailUI {
	Shell shell;

	public MailUI(Shell shell) {
		this.shell=shell;
	}

	public void sendMail(String subject, String body, String recipient, String pdfFilePath) {
		String sender = CoreHub.localCfg.get(PreferenceConstants.SMTP_USER, "");
		String smtpserver = CoreHub.localCfg.get(PreferenceConstants.SMTP_HOST, "localhost");
		String smtppwd = CoreHub.localCfg.get(PreferenceConstants.SMTP_PWD, "doesntMatter");
		String smtpport = CoreHub.localCfg.get(PreferenceConstants.SMTP_PORT, "53");
		Mailer mailer = new Mailer(sender, smtpserver, smtppwd, smtpport);
		MailDialog mailDialog = new MailDialog(shell, recipient);
		mailDialog.sender = sender;
		mailDialog.body = body;
		mailDialog.subject = subject;
		if (mailDialog.open() == Dialog.OK) {
			sender = mailDialog.sender;
			subject = mailDialog.subject;
			body = mailDialog.body;

			try {
				mailer.simpleMail(recipient, subject, body, new String[] { pdfFilePath });
			} catch (Exception e) {
				ExHandler.handle(e);
				SWTHelper.showError("Fehler beim Senden der Mail", e.getMessage());
			}
		}

	}
}

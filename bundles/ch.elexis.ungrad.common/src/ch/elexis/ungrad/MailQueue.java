package ch.elexis.ungrad;

import java.util.ArrayList;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

public class MailQueue {
	static private class Mail {
		String subject;
		String body;
		String recipient;
		String[] attachments;

		Mail(String s, String b, String r, String[] a) {
			subject = s;
			body = b;
			recipient = r;
			attachments = a;
		}
	}

	private ArrayList<Mail> mails = new ArrayList<MailQueue.Mail>();
	String user = CoreHub.localCfg.get(PreferenceConstants.SMTP_USER, "");
	String sender = CoreHub.localCfg.get(PreferenceConstants.MAIL_SENDER, user);
	String smtpserver = CoreHub.localCfg.get(PreferenceConstants.SMTP_HOST, "localhost");
	String smtppwd = CoreHub.localCfg.get(PreferenceConstants.SMTP_PWD, "doesntMatter");
	String smtpport = CoreHub.localCfg.get(PreferenceConstants.SMTP_PORT, "53");

	public void addMail(String subject, String body, String recipient, String[] attachments) {
		mails.add(new Mail(subject, body, recipient, attachments));
	}
	public Result<String> sendMails(){
		Result<String>ret=new Result<String>();
		for(Mail mail:mails) {
			ret.addMessage(SEVERITY.OK, mail.recipient);
		}
		return ret;
	}
}

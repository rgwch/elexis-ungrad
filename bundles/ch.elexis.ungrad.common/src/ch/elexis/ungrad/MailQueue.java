package ch.elexis.ungrad;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.Mailer.SendJob;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
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

	class SendJob extends Job {
		Result<String> ret = new Result<String>();
		String user = CoreHub.localCfg.get(PreferenceConstants.SMTP_USER, "");
		String sender = CoreHub.localCfg.get(PreferenceConstants.MAIL_SENDER, user);
		String smtpHost = CoreHub.localCfg.get(PreferenceConstants.SMTP_HOST, "localhost");
		String smtpPassword = CoreHub.localCfg.get(PreferenceConstants.SMTP_PWD, "doesntMatter");
		String smtpPort = CoreHub.localCfg.get(PreferenceConstants.SMTP_PORT, "53");

		SendJob() {
			super("send invoices");
			setUser(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Sending mails", mails.size());
			for (Mail mail : mails) {
				try {
					sendMail(mail);
					monitor.worked(1);
				} catch (Exception ex) {
					ExHandler.handle(ex);
					SWTHelper.showError("SMTP Mailer", ex.getMessage());
					return new Status(Status.ERROR, "Mailer", ex.getMessage());

				}
			}
			monitor.done();
			return Status.OK_STATUS;

		}

		private void sendMail(Mail mail) throws MessagingException {
			// System.out.println("TLSEmail Start");
			Properties props = new Properties();
			props.put("mail.smtp.host", smtpHost); // SMTP Host
			props.put("mail.smtp.port", smtpPort /* "587" */ );
			props.put("mail.smtp.auth", "true"); // enable authentication
			props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS
		    props.put("mail.smtp.ssl.protocols", "TLSv1.2");
			props.put("mail.smtp.ssl.trust", smtpHost);




			// create Authenticator object to pass in Session.getInstance argument
			Authenticator auth = new Authenticator() {
				// override the getPasswordAuthentication method
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, smtpPassword);
				}
			};
			Session session = Session.getInstance(props, auth);
	
			MimeMessage msg = new MimeMessage(session);
			// set message headers
			msg.addHeader("Content-type", "text/html; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress(sender));
			msg.addRecipient(RecipientType.BCC, msg.getFrom()[0]);
			msg.setReplyTo(msg.getFrom());

			msg.setSubject(mail.subject, "UTF-8");
			BodyPart messageBodyPart = new MimeBodyPart();

			// Fill the message
			if (mail.body.contains("<")) {
				messageBodyPart.setContent(mail.body, "text/html; charset=UTF-8");
			} else {
				messageBodyPart.setText(mail.body);
			}

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			for (String filename : mail.attachments) {
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(filename);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(Util.reduceCharset(FileTool.getFilename(filename)));
				multipart.addBodyPart(messageBodyPart);
			}

			msg.setContent(multipart);
			msg.setSentDate(new Date());
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.recipient, false));
			Transport.send(msg);
		}

	}

	private ArrayList<Mail> mails = new ArrayList<MailQueue.Mail>();

	public void addMail(String subject, String body, String recipient, String[] attachments) {
		mails.add(new Mail(subject, body, recipient, attachments));
	}

	public void sendMails() {
		if (mails.size() > 0) {
			new SendJob().schedule();
		}
	}
}

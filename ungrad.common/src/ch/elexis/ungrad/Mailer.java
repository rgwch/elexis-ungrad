package ch.elexis.ungrad;

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
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;

public class Mailer {
	String from;
	String smtpHost;
	String smtpPassword;
	String smtpPort;

	public Mailer(String from, String smtpHost, String smtpPassword, String smtpPort) {
		this.from = from;
		this.smtpHost = smtpHost;
		this.smtpPassword = smtpPassword;
		this.smtpPort = smtpPort;
	}

	public void sendEmail(Session session, String toEmail, String subject, String body, String[] attachments)
			throws Exception {
		MimeMessage msg = new MimeMessage(session);
		// set message headers
		msg.addHeader("Content-type", "text/html; charset=UTF-8");
		msg.addHeader("format", "flowed");
		msg.addHeader("Content-Transfer-Encoding", "8bit");

		msg.setFrom(new InternetAddress(from));
		msg.addRecipient(RecipientType.BCC, msg.getFrom()[0]);
		msg.setReplyTo(msg.getFrom());
	
		msg.setSubject(subject, "UTF-8");
		if (attachments != null) {
			BodyPart messageBodyPart = new MimeBodyPart();

			// Fill the message
			if (body.contains("<")) {
				messageBodyPart.setContent(body, "text/html; charset=UTF-8");
			} else {
				messageBodyPart.setText(body);
			}

			// Create a multipart message for attachment
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			for (String filename : attachments) {
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(filename);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(Util.reduceCharset(FileTool.getFilename(filename)));
				multipart.addBodyPart(messageBodyPart);
			}
			// Send the complete message parts
			msg.setContent(multipart);
		} else {
			msg.setText(body, "UTF-8");
		}
		msg.setSentDate(new Date());
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
		Transport.send(msg);
		SWTHelper.showInfo("Mail gesendet", "Die Mail wurde an " + toEmail + " gesendet.");
	}

	public void simpleMail(String to, String subject, String body, String[] attachments) throws Exception {

		Properties props = System.getProperties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", smtpPort);
		Session session = Session.getInstance(props, null);
		sendEmail(session, to, subject, body, attachments);
	}

	public void tlsMail(String to, String subject, String body, String[] attachments) throws Exception {

		System.out.println("TLSEmail Start");
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost); // SMTP Host
		props.put("mail.smtp.port", smtpPort /* "587" */ ); // TLS Port
		props.put("mail.smtp.auth", "true"); // enable authentication
		props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS

		// create Authenticator object to pass in Session.getInstance argument
		Authenticator auth = new Authenticator() {
			// override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, smtpPassword);
			}
		};
		Session session = Session.getInstance(props, auth);
		sendEmail(session, to, subject, body, attachments);
	}

	public void sslMail(String to, String subject, String body, String[] attachments) throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost); // SMTP Host
		props.put("mail.smtp.socketFactory.port", smtpPort /* "465" */); // SSL Port
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // SSL Factory Class
		props.put("mail.smtp.auth", "true"); // Enabling SMTP Authentication
		props.put("mail.smtp.port", smtpPort /* "465" */); // SMTP Port

		Authenticator auth = new Authenticator() {
			// override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, smtpPassword);
			}
		};

		Session session = Session.getDefaultInstance(props, auth);
		System.out.println("Session created");
		sendEmail(session, to, subject, body, attachments);

	}

}
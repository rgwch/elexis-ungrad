/*******************************************************************************
 * Copyright (c) 2016-2024 by G. Weirich
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

package ch.elexis.ungrad;

import java.util.Date;
import java.util.Properties;


import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.io.FileTool;

/**
 * Send Mails via SMTP
 * 
 * @author gerry
 *
 */
public class Mailer {
	String sender;
	String smtpHost;
	String smtpPassword;
	String smtpPort;

	public Mailer(String from, String smtpHost, String smtpPassword, String smtpPort) {
		this.sender = from;
		this.smtpHost = smtpHost;
		this.smtpPassword = smtpPassword;
		this.smtpPort = smtpPort;
	}

	class SendJob extends Job {
		private MimeMessage msg;
		private String address;

		public SendJob(MimeMessage msg, String adr) {
			super("Send Mail");
			this.msg = msg;
			address = adr;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				monitor.beginTask("Sending mail " + msg.getMessageID(), 1);
				Transport.send(msg);
				monitor.done();
				SWTHelper.showInfo("Mail gesendet", "Die Mail wurde an " + address + " gesendet.");
				return Status.OK_STATUS;
			} catch (Exception ex) {
				SWTHelper.showError("SMTP Mailer", ex.getMessage());
				return new Status(Status.ERROR, "Mailer", ex.getMessage());
			}

		}

	}

	public void sendEmail(Session session, String toEmail, String subject, String body, String[] attachments)
			throws Exception {
		MimeMessage msg = new MimeMessage(session);
		// set message headers
		msg.addHeader("Content-type", "text/html; charset=UTF-8");
		msg.addHeader("format", "flowed");
		msg.addHeader("Content-Transfer-Encoding", "8bit");

		msg.setFrom(new InternetAddress(sender));
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

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);


			for (String filename : attachments) {
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(filename);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(Util.reduceCharset(FileTool.getFilename(filename)));
				multipart.addBodyPart(messageBodyPart);
			}

			msg.setContent(multipart);
		} else {
			msg.setText(body, "UTF-8");
		}
		msg.setSentDate(new Date());
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
		SendJob sendJob = new SendJob(msg, toEmail);
		sendJob.setUser(true);
		sendJob.schedule();
	}

	/**
	 * Send without security (e.g. to al local smpt server like HIN Client
	 * 
	 * @param to
	 * @param subject
	 * @param body
	 * @param attachments
	 * @throws Exception
	 */
	public void simpleMail(String to, String subject, String body, String[] attachments) throws Exception {

		Properties props = System.getProperties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", smtpPort);
		Session session = Session.getInstance(props, null);
		sendEmail(session, to, subject, body, attachments);
	}

	/**
	 * Send via Starttls Connection
	 * 
	 * @param to
	 * @param subject
	 * @param body
	 * @param attachments
	 * @throws Exception
	 */
	public void tlsMail(String user, String to, String subject, String body, String[] attachments) throws Exception {

		System.out.println("TLSEmail Start");
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost); // SMTP Host
		props.put("mail.smtp.port", smtpPort /* "587" */ );
		props.put("mail.smtp.auth", "true"); // enable authentication
		props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS

		// create Authenticator object to pass in Session.getInstance argument
		Authenticator auth = new Authenticator() {
			// override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, smtpPassword);
			}
		};
		Session session = Session.getInstance(props, auth);
		sendEmail(session, to, subject, body, attachments);
	}

	/**
	 * Send via SSL Connection
	 * 
	 * @param to
	 * @param subject
	 * @param body
	 * @param attachments
	 * @throws Exception
	 */
	public void sslMail(String user, String to, String subject, String body, String[] attachments) throws Exception {
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost); // SMTP Host
		props.put("mail.smtp.socketFactory.port", smtpPort /* "465" */); // SSL Port
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // SSL Factory Class
		props.put("mail.smtp.auth", "true"); // Enabling SMTP Authentication
		props.put("mail.smtp.port", smtpPort /* "465" */); // SMTP Port

		Authenticator auth = new Authenticator() {
			// override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, smtpPassword);
			}
		};

		Session session = Session.getDefaultInstance(props, auth);
		System.out.println("Session created");
		sendEmail(session, to, subject, body, attachments);

	}

}

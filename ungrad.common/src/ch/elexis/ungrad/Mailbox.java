package ch.elexis.ungrad;

import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage.RecipientType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import ch.elexis.core.data.activator.CoreHub;

import javax.mail.Message;

public class Mailbox {
	private static final Logger logger = LoggerFactory.getLogger(Mailbox.class);

	public void fetch() {
		String hostname=CoreHub.localCfg.get(PreferenceConstants.IMAP_HOST, "");
		String username=CoreHub.localCfg.get(PreferenceConstants.IMAP_USER, "");
		String pwd=CoreHub.localCfg.get(PreferenceConstants.IMAP_PWD, "");
		String port=CoreHub.localCfg.get(PreferenceConstants.IMAP_PORT, "");
		readInboundEmails(hostname, Integer.parseInt(port), username, pwd);
	}
	public void readInboundEmails(String hostname, int port, String unsername, String password) {
		// create session object
		Session session = this.getImapSession(hostname, port);
		try {
			// connect to message store
			Store store = session.getStore("imap");
			store.connect(hostname, port, unsername, password);
			// open the inbox folder
			IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
			inbox.open(Folder.READ_WRITE);
			// fetch messages
			Message[] messages = inbox.getMessages();
			// read messages
			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];
				Address[] fromAddress = msg.getFrom();
				String from = fromAddress[0].toString();
				String subject = msg.getSubject();
				Address[] toList = msg.getRecipients(RecipientType.TO);
				Address[] ccList = msg.getRecipients(RecipientType.CC);
				String contentType = msg.getContentType();
			}
		} catch (AuthenticationFailedException e) {
			logger.error("Exception in reading EMails : " + e.getMessage());
		} catch (MessagingException e) {
			logger.error("Exception in reading EMails : " + e.getMessage());
		} catch (Exception e) {
			logger.error("Exception in reading EMails : " + e.getMessage());
		}
	}

	private Session getImapSession(String host, int port) {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imap");
		props.setProperty("mail.debug", "true");
		props.setProperty("mail.imap.host", host);
		props.setProperty("mail.imap.port", Integer.toString(port, 10));
		props.setProperty("mail.imap.ssl.enable", "true");
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);
		return session;
	}
}

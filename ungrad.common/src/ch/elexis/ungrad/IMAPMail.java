package ch.elexis.ungrad;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class IMAPMail {

	public void fetch(String[] whitelist) throws Exception {
		Folder folder = null;
		Store store = null;
		String host = CoreHub.localCfg.get(PreferenceConstants.IMAP_HOST, "");
		String user = CoreHub.localCfg.get(PreferenceConstants.IMAP_USER, "");
		String pwd = CoreHub.localCfg.get(PreferenceConstants.IMAP_PWD, "");
		// String port = CoreHub.localCfg.get(PreferenceConstants.IMAP_PORT, "993");
		long uidvalidity = Long.parseLong(CoreHub.localCfg.get(PreferenceConstants.IMAP_UIDVALIDITY, "0"));

		long lastseen = Long.parseLong(CoreHub.localCfg.get(PreferenceConstants.IMAP_LAST_SEEN, "1"));
		// System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		// props.setProperty("mail.imap.auth.login.disable", "true");
		// props.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);
		store = session.getStore("imaps");
		store.connect(host, 993, user,null);
		folder = store.getFolder("Inbox");
		if (folder.exists()) {
			folder.open(Folder.READ_ONLY);
			UIDFolder uf = (UIDFolder) folder;
			long uid = uf.getUIDValidity();
			if (uid != uidvalidity) {
				lastseen = 1;
				uidvalidity = uid;
				CoreHub.localCfg.set(PreferenceConstants.IMAP_UIDVALIDITY, Long.toString(uidvalidity, 10));
				CoreHub.localCfg.flush();
			}
			// Message m1=uf.getMessageByUID(1);
			Message[] messages = uf.getMessagesByUID(lastseen, -1);// folder.getMessages();
			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];
				System.out.println(uf.getUID(msg));
				if (!msg.isSet(Flags.Flag.SEEN)) {
					Address[] from = msg.getFrom();
					if (from.length == 0) {
						from = msg.getReplyTo();
					}
					if (from.length > 0) {
						InternetAddress iadr = (InternetAddress) from[0];
						String sender = findSender(iadr.getAddress(), whitelist);
						if (sender != null) {
							System.out.println(sender + " " + msg.getMessageNumber());
						}
					}
				}
				lastseen = uf.getUID(msg);
				CoreHub.localCfg.set(PreferenceConstants.IMAP_LAST_SEEN, Long.toString(lastseen, 10));
				CoreHub.localCfg.flush();
			}
			folder.close(false);
		}
		store.close();
	}

	String findSender(String addr, String[] whitelist) {
		if (whitelist == null || whitelist.length == 0) {
			return addr;
		}
		for (String wl : whitelist) {
			String[] w = wl.split(":");
			if (w[0].startsWith("@")) {
				int idx = addr.indexOf("@");
				if (w[0].substring(1).equalsIgnoreCase(addr.substring(idx + 1))) {
					return w.length == 2 ? w[1] : addr;
				}
			} else {
				if (w[0].equalsIgnoreCase(addr)) {
					return w.length == 2 ? w[1] : addr;
				}
			}
		}
		return null;
	}
}

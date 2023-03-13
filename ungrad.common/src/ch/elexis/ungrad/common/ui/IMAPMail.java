package ch.elexis.ungrad.common.ui;

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
import ch.elexis.ungrad.PreferenceConstants;
import ch.rgw.tools.ExHandler;

public class IMAPMail {

	public void fetch() throws Exception {
		Folder folder = null;
		Store store = null;
		String host = CoreHub.localCfg.get(PreferenceConstants.IMAP_HOST, "");
		String user = CoreHub.localCfg.get(PreferenceConstants.IMAP_USER, "");
		String pwd = CoreHub.localCfg.get(PreferenceConstants.IMAP_PWD, "");
		String port = CoreHub.localCfg.get(PreferenceConstants.IMAP_PORT, "");
		long uidvalidity = Long.parseLong(CoreHub.localCfg.get(PreferenceConstants.IMAP_UIDVALIDITY, "0"));;
		long lastseen = Long.parseLong(CoreHub.localCfg.get(PreferenceConstants.IMAP_LAST_SEEN, "0"));
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");

		Session session = Session.getDefaultInstance(props, null);
		// session.setDebug(true);
		store = session.getStore("imaps");
		store.connect(host, user, pwd);
		folder = store.getFolder("Inbox");
		if (folder.exists()) {
			folder.open(Folder.READ_ONLY);
			UIDFolder uf = (UIDFolder) folder;
			long uid = uf.getUIDValidity();
			if(uid!=uidvalidity) {
				lastseen=0;
				uidvalidity=uid;
				CoreHub.localCfg.set(PreferenceConstants.IMAP_UIDVALIDITY, Long.toString(uidvalidity,10));
				CoreHub.localCfg.flush();
			}
			// Message m1=uf.getMessageByUID(1);
			Message[] messages = uf.getMessagesByUID(lastseen, -1);//folder.getMessages();
			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];
				System.out.println(uf.getUID(msg));
				if (!msg.isSet(Flags.Flag.SEEN)) {
					Address[] from = msg.getFrom();
					if (from.length == 0) {
						from = msg.getReplyTo();
					}
					if (from.length > 0) {
						InternetAddress iadr=(InternetAddress) from[0];
						String sender = iadr.getAddress();
						System.out.println(sender + " " + msg.getMessageNumber());
					}
				}
				lastseen=uf.getUID(msg);
				CoreHub.localCfg.set(PreferenceConstants.IMAP_LAST_SEEN, Long.toString(lastseen,10));
				CoreHub.localCfg.flush();
			}
			folder.close(false);
		}
		store.close();
	}
}

package ch.elexis.ungrad;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.URLName;
import com.sun.mail.mbox.*;

public class MBox {
	private final Path path; // Path to .mbox file
	private Provider mbox;

	public MBox() {
		this.path = Paths.get("/home/gerry/.thunderbird/9n40ugmr.default/ImapMail/mail.weirich.ch/INBOX");

		// this.path =
		// Paths.get("/home/gerry/.thunderbird/vvdxj3mb.default-release/ImapMail/imap.mail.hin.ch/INBOX");
		// mbox=new Provider(Provider.Type.STORE, "mbox", "com.sun.mail.mbox.MboxStore",
		// "Oracle", "3.2.1");
		mbox = new MboxProvider();
	}

	public Message[] readMessages() throws NoSuchProviderException {
		Message[] messages = new Message[0];
		URLName server = new URLName("mbox:" + path.toString());
		Properties props = new Properties();
		props.setProperty("mail.mbox.locktype", "none");
		props.setProperty("mail.mime.address.strict", "false");
		// props.setProperty("mail.store.protocol", "mstore");
		Session session = Session.getDefaultInstance(props);
		// Session session=Session.getInstance(props);
		// session.setProvider(mbox);
		try {
			Folder folder = session.getFolder(server);
			folder.open(Folder.READ_ONLY);
			messages = folder.getMessages();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
}

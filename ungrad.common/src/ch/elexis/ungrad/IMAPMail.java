/*******************************************************************************
 * Copyright (c) 2023, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;

public class IMAPMail {
	private String[] whitelist;
	Folder folder;
	Session session;
	Store store;

	public IMAPMail(String[] whitelist) {
		this.whitelist = whitelist;
	}

	public Map<String, byte[]> fetch() throws Exception {
		Map<String, byte[]> ret = new HashMap<String, byte[]>();
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
		session = Session.getDefaultInstance(props, null);
		session.setDebug(true);
		store = session.getStore("imaps");
		store.connect(host, user, pwd);
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
			Message[] messages = uf.getMessagesByUID(lastseen+1, -1);// folder.getMessages();
			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];
				System.out.println(uf.getUID(msg));
				Address[] from = msg.getFrom();
				if (from.length == 0) {
					from = msg.getReplyTo();
				}
				if (from.length > 0) {
					InternetAddress[] iadr = (InternetAddress[]) from;
					String sender = findSender(iadr);
					if (sender != null) {
						System.out.println(sender + " " + msg.getMessageNumber());
						saveParts(msg.getContent(), ret);
					}
				}
				lastseen = uf.getUID(msg);
				CoreHub.localCfg.set(PreferenceConstants.IMAP_LAST_SEEN, Long.toString(lastseen, 10));
				CoreHub.localCfg.flush();
			}
			folder.close(false);
		}
		store.close();
		return ret;
	}

	String findSender(InternetAddress[] addr) {
		String ret = null;
		for (InternetAddress adr : addr) {
			if (adr.getAddress().contains("@")) {
				ret = adr.getAddress();
				break;
			}
		}
		if (whitelist == null || whitelist.length == 0) {
			return ret;
		}
		for (String wl : whitelist) {
			String[] w = wl.split(":");
			if (w[0].startsWith("@")) {
				int idx = ret.indexOf("@");
				if (w[0].substring(1).equalsIgnoreCase(ret.substring(idx + 1))) {
					return w.length == 2 ? w[1] : ret;
				}
			} else {
				if (w[0].equalsIgnoreCase(ret)) {
					return w.length == 2 ? w[1] : ret;
				}
			}
		}
		return null;
	}

	/**
	 * Extract PDF Attachments from Multipart messages
	 * 
	 * @param content
	 * @param attachments
	 * @throws Exception
	 */
	void saveParts(Object content, Map<String, byte[]> attachments) throws Exception {
		if (content instanceof Multipart) {
			Multipart multi = (Multipart) content;
			int parts = multi.getCount();
			for (int j = 0; j < parts; ++j) {
				MimeBodyPart part = (MimeBodyPart) multi.getBodyPart(j);
				if (part.getContent() instanceof Multipart) {
					// part-within-a-part, do some recursion...
					saveParts(part.getContent(), attachments);
				} else {
					String extension = "";
					String fn = part.getFileName();
					if (!StringTool.isNothing(fn)) {
						String f2 = MimeUtility.decodeText(fn);
						if (f2.toLowerCase().endsWith("pdf")) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							InputStream is = part.getInputStream();
							FileTool.copyStreams(is, baos);
							attachments.put(f2, baos.toByteArray());
						}
					}

				}

			}
		}
	}

}

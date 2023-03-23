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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;

public class IMAPMail {
	private String[] whitelist;
	Folder folder;
	Session session;
	Store store;
	long uidvalidity;
	long lastseen;
	INotifier notifier;

	public static interface INotifier {
		public void documentFound(String name, byte[] doc);
	}

	public IMAPMail(String[] whitelist, INotifier notify) {
		this.whitelist = whitelist;
		this.notifier = notify;
	}

	class FetchJob extends Job {

		public FetchJob() {
			super("Import Imap Mails");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
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
				Message[] messages = uf.getMessagesByUID(lastseen + 1, -1);// folder.getMessages();
				monitor.beginTask("Reading IMAP", messages.length);
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
							saveParts(msg.getContent());
						}
					}
					lastseen = uf.getUID(msg);
					CoreHub.localCfg.set(PreferenceConstants.IMAP_LAST_SEEN, Long.toString(lastseen, 10));
					CoreHub.localCfg.flush();
					monitor.worked(1);
					if (monitor.isCanceled()) {
						folder.close(false);
						store.close();
						return Status.CANCEL_STATUS;
					}
				}
				folder.close(false);
				store.close();
				monitor.done();
				return Status.OK_STATUS;
			} catch (Exception ex) {
				return new Status(Status.ERROR, "Imapmail", ex.getMessage());
			}
		}

	}

	public void fetch() throws Exception {
		Map<String, byte[]> ret = new HashMap<String, byte[]>();
		String host = CoreHub.localCfg.get(PreferenceConstants.IMAP_HOST, "");
		String user = CoreHub.localCfg.get(PreferenceConstants.IMAP_USER, "");
		String pwd = CoreHub.localCfg.get(PreferenceConstants.IMAP_PWD, "");
		// String port = CoreHub.localCfg.get(PreferenceConstants.IMAP_PORT, "993");
		uidvalidity = Long.parseLong(CoreHub.localCfg.get(PreferenceConstants.IMAP_UIDVALIDITY, "0"));
		lastseen = Long.parseLong(CoreHub.localCfg.get(PreferenceConstants.IMAP_LAST_SEEN, "1"));
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		session = Session.getDefaultInstance(props, null);
		// session.setDebug(true);
		store = session.getStore("imaps");
		store.connect(host, user, pwd);
		folder = store.getFolder("Inbox");
		if (folder.exists()) {
			FetchJob fetcher = new FetchJob();
			fetcher.setUser(true);
			fetcher.schedule();
		}
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
	void saveParts(Object content) throws Exception {
		if (content instanceof Multipart) {
			Multipart multi = (Multipart) content;
			int parts = multi.getCount();
			for (int j = 0; j < parts; ++j) {
				MimeBodyPart part = (MimeBodyPart) multi.getBodyPart(j);
				if (part.getContent() instanceof Multipart) {
					// part-within-a-part, do some recursion...
					saveParts(part.getContent());
				} else {
					String extension = "";
					String fn = part.getFileName();
					if (!StringTool.isNothing(fn)) {
						String f2 = MimeUtility.decodeText(fn);
						String f3=f2.toLowerCase();
						if (f3.matches(".+\\.(jpe?g|pdf)")) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							InputStream is = part.getInputStream();
							FileTool.copyStreams(is, baos);
							notifier.documentFound(f2, baos.toByteArray());
						}
					}

				}

			}
		}
	}

}

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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class MBox {
	final static String p = "/home/gerry/.thunderbird/3a33d0y1.default/ImapMail/imap.mail.hin.ch/INBOX";
	private final File mbox; // Path to .mbox file

	public MBox() throws Exception {
		this(p);
	}

	public MBox(String file) throws Exception {

		this.mbox = new File(file);
		if (!mbox.exists() || !mbox.isFile() || !mbox.canRead()) {
			throw new Exception("Can't read mbox");
		}
	}

	/**
	 * Read all messages and return pdf attachments of those - with a sender
	 * mentioned in whitelist - received today
	 * 
	 * @param whitelist
	 * @return
	 * @throws Exception
	 */
	public Map<String, byte[]> readMessages(String[] whitelist) throws Exception {
		Map<String, byte[]> ret = new HashMap<String, byte[]>();
		Session session = Session.getDefaultInstance(new Properties());
		InputStream in = new FileInputStream(mbox);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder msg = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("From ")) {
				if (msg.length() > 0) {
					String sMsg = msg.toString();
					StringBufferInputStream strin = new StringBufferInputStream(sMsg);
					MimeMessage m = new MimeMessage(session, strin);
					TimeTool date = new TimeTool(m.getReceivedDate());
					if (date.isSameDay(new TimeTool())) {
						Address[] from = m.getFrom();
						if (from.length == 0) {
							from = m.getReplyTo();
						}
						if (from.length > 0) {
							InternetAddress[] iadr = (InternetAddress[]) from;
							String sender = findSender(iadr, whitelist);
							if (sender != null) {
								System.out.println(sender);
								saveParts(m.getContent(), ret);
							}
						}
					}
					msg.setLength(0);
				}
			}
			msg.append(line).append("\r\n");
		}
		return ret;
	}

	String findSender(InternetAddress[] addr, String[] whitelist) {
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

/*******************************************************************************
 * Copyright (c) 2022-2025 by G. Weirich
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

package ch.elexis.ungrad.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.common.messages";
	
	// Plugin metadata
	public static String Plugin_PreferencesName;
	public static String Plugin_PreferencesName_ExtInfo;
	public static String Plugin_ViewName;
	
	// Preferences
	public static String Preferences_Description;
	public static String Preferences_DocDirectory;
	public static String Preferences_SMTPConnection;
	public static String Preferences_ConnectionPlain;
	public static String Preferences_ConnectionTLS;
	public static String Preferences_ConnectionSSL;
	public static String Preferences_MailSender;
	public static String Preferences_SMTPServer;
	public static String Preferences_SMTPPort;
	public static String Preferences_SMTPUser;
	public static String Preferences_SMTPPassword;
	public static String Preferences_IMAPServer;
	public static String Preferences_IMAPPort;
	public static String Preferences_IMAPUser;
	public static String Preferences_IMAPPassword;
	public static String Preferences_UseAI;
	public static String Preferences_AIUrl;
	
	// ExtIdPreferences
	public static String ExtIdPreferences_SelectContact;
	public static String ExtIdPreferences_SelectContactMessage;
	public static String ExtIdPreferences_AddKey;
	public static String ExtIdPreferences_DeleteEntry;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages() {
	}
}

/*******************************************************************************
 * Copyright (c) 2023-2025, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.inbox.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.inbox.ui.messages";

	public static String ImportDocumentDialog_Directory;

	public static String ImportDocumentDialog_SelectFileTitle;
	
	public static String View_Action_FetchMail_Title;
	public static String View_Action_FetchMail_Tooltip;
	public static String View_Action_Assign_Title;
	public static String View_Action_Assign_Tooltip;
	public static String View_Action_Delete_Title;
	public static String View_Action_Delete_Tooltip;
	public static String View_Action_Open_Title;
	public static String View_Action_Open_Tooltip;
	public static String View_Action_Reload_Title;
	public static String View_Action_Reload_Tooltip;
	public static String View_Error_CouldNotStart;
	public static String View_Error_FetchingMail;
	public static String View_Error_Writing;
	public static String View_Error_Moving;
	
	public static String ImportDocumentDialog_Title;
	public static String ImportDocumentDialog_Message;
	public static String ImportDocumentDialog_ShellTitle;
	public static String ImportDocumentDialog_AssignPatient;
	public static String ImportDocumentDialog_SelectPatientTitle;
	public static String ImportDocumentDialog_SelectPatientMessage;
	public static String ImportDocumentDialog_ErrorNoPatient;
	public static String ImportDocumentDialog_UseAI;
	
	public static String Preferences_Description;
	public static String Preferences_Directory;
	public static String Preferences_FetchEmails;
	public static String Preferences_MBoxFile;
	public static String Preferences_Whitelist;
	public static String Preferences_FilenameAnalysis;
	public static String Preferences_AnalyzeContents;
	public static String Preferences_AnalyzeContentsTooltip;
	public static String Preferences_RegexpTester;
	public static String Preferences_Regexp;
	public static String Preferences_String;
	public static String Preferences_Found;
	public static String Preferences_Matches;
	public static String Preferences_NoMatch;
	
	public static String Preferences_FetchMethod_None;
	public static String Preferences_FetchMethod_IMAP;
	public static String Preferences_FetchMethod_MBox;
	
	public static String Plugin_ViewName;
	public static String Plugin_PreferencesName;
	
	public static String Controller_Error_AICall;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages() {
	}
}

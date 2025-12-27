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

package ch.elexis.ungrad.labview;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.labview.messages";
	
	// Plugin metadata
	public static String Plugin_ViewName;
	public static String Plugin_PreferencesName;
	
	// LaborView - Tab titles
	public static String LaborView_Tab_Compact;
	public static String LaborView_Tab_Synopsis;
	public static String LaborView_Tab_Full;
	
	// LaborView - Actions
	public static String LaborView_Action_Export_Tooltip;
	public static String LaborView_Action_ViewBrowser_Tooltip;
	public static String LaborView_Action_Import_Title;
	public static String LaborView_Action_Import_Tooltip;
	public static String LaborView_Action_Import_Message;
	public static String LaborView_Action_Import_ShellTitle;
	public static String LaborView_Action_Import_DialogTitle;
	public static String LaborView_Action_Cleanup_Title;
	public static String LaborView_Action_Cleanup_Tooltip;
	public static String LaborView_Action_Cleanup_DialogTitle;
	public static String LaborView_Action_Cleanup_DialogMessage;
	
	// PreferencePage
	public static String PreferencePage_Description;
	public static String PreferencePage_Template;
	public static String PreferencePage_Exclude;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages() {
	}
}

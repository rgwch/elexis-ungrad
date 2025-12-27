/*******************************************************************************
 * Copyright (c) 2018-2025 by G. Weirich
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

package ch.elexis.ungrad.labenter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.labenter.messages";
	
	// Plugin metadata
	public static String Plugin_ViewName;
	public static String Plugin_PreferencesName;
	
	// ManualLabEntry
	public static String ManualLabEntry_NoPatientSelected;
	public static String ManualLabEntry_LabOf;
	public static String ManualLabEntry_From;
	public static String ManualLabEntry_Action_ChangeDate_Title;
	public static String ManualLabEntry_Action_ChangeDate_Tooltip;
	public static String ManualLabEntry_Action_Send_Title;
	public static String ManualLabEntry_Action_Send_Tooltip;
	public static String ManualLabEntry_Action_Clear_Title;
	public static String ManualLabEntry_Action_Clear_Tooltip;
	public static String ManualLabEntry_Dialog_EnterData_Title;
	public static String ManualLabEntry_Dialog_EnterData_Message;
	public static String ManualLabEntry_Dialog_Success;
	public static String ManualLabEntry_Title;
	
	// LabEntryTable
	public static String LabEntryTable_Column_Parameter;
	public static String LabEntryTable_Column_Value;
	
	// PreferencePage
	public static String PreferencePage_Description;
	public static String PreferencePage_LabItems;
	
	// LabItemSelector
	public static String LabItemSelector_Message;
	public static String LabItemSelector_Title;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages() {
	}
}

/*******************************************************************************
 * Copyright (c) 2007-2024, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.buchhaltung.kassenbuch;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.buchhaltung.kassenbuch.messages";
	
	// Plugin metadata
	public static String Plugin_ViewName;
	public static String Plugin_CategoryName;
	
	// KassenView
	public static String KassenView_DisplayAllBookings;
	public static String KassenView_DisplayFromTo;
	public static String KassenView_ColumnBeleg;
	public static String KassenView_ColumnDatum;
	public static String KassenView_ColumnSoll;
	public static String KassenView_ColumnHaben;
	public static String KassenView_ColumnSaldo;
	public static String KassenView_ColumnKategorie;
	public static String KassenView_ColumnText;
	
	public static String KassenView_Action_Income_Title;
	public static String KassenView_Action_Income_Tooltip;
	public static String KassenView_Action_Expense_Title;
	public static String KassenView_Action_Expense_Tooltip;
	public static String KassenView_Action_Storno_Title;
	public static String KassenView_Action_Storno_Tooltip;
	public static String KassenView_Action_Balance_Title;
	public static String KassenView_Action_Balance_Tooltip;
	public static String KassenView_Action_Period_Title;
	public static String KassenView_Action_Period_Tooltip;
	public static String KassenView_Action_Print_Title;
	public static String KassenView_Action_Print_Tooltip;
	public static String KassenView_Action_EditCategories_Title;
	public static String KassenView_Action_EditCategories_Tooltip;
	
	public static String KassenView_Balance_DialogTitle;
	public static String KassenView_Balance_DialogMessage;
	public static String KassenView_Balance_DefaultValue;
	public static String KassenView_Balance_Check;
	public static String KassenView_Balance_Shortage;
	public static String KassenView_Balance_Surplus;
	public static String KassenView_Error_Title;
	public static String KassenView_Error_InvalidAmount;
	public static String KassenView_Error_PrintFailed;
	
	// BuchungsDialog
	public static String BuchungsDialog_Income_Title;
	public static String BuchungsDialog_Expense_Title;
	public static String BuchungsDialog_Edit_Title;
	public static String BuchungsDialog_Message;
	public static String BuchungsDialog_ShellTitle;
	public static String BuchungsDialog_Label_Beleg;
	public static String BuchungsDialog_Label_Date;
	public static String BuchungsDialog_Label_Amount;
	public static String BuchungsDialog_Label_Category;
	public static String BuchungsDialog_Label_Text;
	
	// EditCatsDialog
	public static String EditCatsDialog_Title;
	public static String EditCatsDialog_Message;
	public static String EditCatsDialog_ShellTitle;
	
	// DatumEingabeDialog
	public static String DatumEingabeDialog_Title;
	public static String DatumEingabeDialog_Message;
	public static String DatumEingabeDialog_ShellTitle;
	public static String DatumEingabeDialog_Label_From;
	public static String DatumEingabeDialog_Label_To;
	
	// KassenbuchDruckDialog
	public static String KassenbuchDruckDialog_ShellTitle;
	public static String KassenbuchDruckDialog_ColumnNr;
	public static String KassenbuchDruckDialog_ColumnDate;
	public static String KassenbuchDruckDialog_ColumnDebit;
	public static String KassenbuchDruckDialog_ColumnCredit;
	public static String KassenbuchDruckDialog_ColumnAmount;
	public static String KassenbuchDruckDialog_ColumnText;
	public static String KassenbuchDruckDialog_Miscellaneous;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages() {
	}
}

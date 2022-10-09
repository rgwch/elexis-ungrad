/*******************************************************************************
 * Copyright (c) 2007-2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.privatrechnung_qr.data;

public class PreferenceConstants {
	public static final String PLUGIN_ID = "ch.berchtold.emanuel.privatrechnung";
	
	// for different incarantations of private billing systems change the following two lines
	public static final String BillingSystemName = "PRIVATRQR";
	public static final String cfgBase = "privatrechnung_qr";
	
	public static final String cfgBank = cfgBase + "/bank";
	public static final String bankClient = cfgBase + "/bankKunde";
	public static final String QRIBAN = cfgBase + "/qrIBAN";
	public static final String TEMPLATE_BILL = cfgBase + "/template_bill";	
	public static final String TEMPLATE_REMINDER1 = cfgBase + "/template_m1";
	public static final String TEMPLATE_REMINDER2 = cfgBase + "/template_m2";
	public static final String TEMPLATE_REMINDER3 = cfgBase + "/template_m3";
	public static final String TEMPLATE_PAGE2 = cfgBase + "/template_page2";
	public static final String AVAILABLE_SPACE_1 = cfgBase +"/space_page1";
	public static final String AVAILABLE_SPACE_2 = cfgBase +"/space_page2";
	
	public static final String RNN_DIR_PDF = cfgBase + "/pdfDir";
	public static final String RNN_DIR_XML = cfgBase + "/xmlDir";
	public static final String DO_PRINT = cfgBase + "/doPrint";
	public static final String DIRECT_PRINT = cfgBase + "/directPrint";
	public static final String DEFAULT_PRINTER = cfgBase + "/defaultPrinter";
	public static final String DELETE_AFTER_PRINT = cfgBase + "/doDelete";
	public static final String DEBUGFILES = cfgBase + "/doKeepDebug";
	public static final String FACE_DOWN = cfgBase + "/doPrintFacedown";

	
}

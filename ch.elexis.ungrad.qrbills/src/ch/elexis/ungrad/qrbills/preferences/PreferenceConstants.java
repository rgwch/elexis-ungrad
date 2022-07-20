/*******************************************************************************
 * Copyright (c) 2018-2022 by G. Weirich
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
package ch.elexis.ungrad.qrbills.preferences;

public class PreferenceConstants {
	static final String BASE = "ch.elexis.ungrad/qrbills";

	// Settings entries
	public static final String RNN_DIR = BASE + "/rnndir";
	public static final String TEMPLATE_BILL = BASE + "/template_bill";
	public static final String TEMPLATE_REMINDER1 = BASE + "/template_reminder1";
	public static final String TEMPLATE_REMINDER2 = BASE + "/template_reminder2";
	public static final String TEMPLATE_REMINDER3 = BASE + "/template_reminder3";
	public static final String DEFAULT_PRINTER = BASE + "/printer";
	public static final String DO_PRINT = BASE + "/do_print";
	public static final String DIRECT_PRINT = BASE + "/direct_print";
	public static final String DELETE_AFTER_PRINT = BASE + "/delete_after_print";

	// Extinfo entries
	public static final String QRBANK = BASE + "/bank";
	public static final String QRIBAN = BASE + "/iban";

}

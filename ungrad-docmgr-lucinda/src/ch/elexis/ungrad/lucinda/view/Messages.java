/*******************************************************************************
 * Copyright (c) 2016-2020 by G. Weirich
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

package ch.elexis.ungrad.lucinda.view;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.lucinda.view.messages"; //$NON-NLS-1$
	public static String GlobalView_11;
	public static String GlobalView_actPatient_name;
	public static String GlobalView_actPatient_tooltip;
	public static String GlobalView_filterInbox_name;
	public static String GlobalView_filterKons_name;
	public static String GlobalView_filterKons_tooltip;
	public static String GlobalView_filterOmni_name;
	public static String GlobalView_filterOmni_tooltip;
	public static String GlobalView_omnivoreImport_Name;
	public static String GlobalView_omnivoreImport_tooltip;
	public static String GlobalView_synckons_Name;
	public static String GlobalView_synckons_tooltip;
	public static String LucindaPrefs_exclude_Metadata;
	public static String LucindaPrefs_msgPrefix;
	public static String LucindaPrefs_netmask;
	public static String LucindaPrefs_serverAddress;
	public static String LucindaPrefs_serverPort;
	public static String LucindaPrefs_documentStore;
	public static String Master_clearButton_tooltip;
	public static String Master_col_caption_date;
	public static String Master_col_caption_doc;
	public static String Master_col_caption_patient;
	public static String Master_col_caption_type;
	public static String Master_connected_tooltip;
	public static String Master_connected_tooltip2;
	public static String Master_disconnected_tooltip;
	public static String Master_searchButton_caption;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

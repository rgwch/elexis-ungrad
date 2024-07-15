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

package ch.elexis.ungrad.lucinda;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.io.Settings;
import ch.rgw.tools.net.NetTool;

public class Preferences {
	public static final Settings cfg = CoreHub.localCfg;
	public static final String MSG = "ch.rgw.lucinda"; //$NON-NLS-1$

	private static final String BASE = "ch.rgw.docmgr-lucinda."; //$NON-NLS-1$
	public static final String NETWORK = BASE + "network"; //$NON-NLS-1$
	public static final String INCLUDE_KONS = BASE + "withKons"; //$NON-NLS-1$
	public static final String INCLUDE_OMNI = BASE + "withOmni"; //$NON-NLS-1$
	public static final String LASTSCAN_OMNI = BASE + "omniLast"; //$NON-NLS-1$
	public static final String LASTSCAN_KONS = BASE + "konsLast"; //$NON-NLS-1$
	public static final String EXCLUDEMETA = BASE + "excludemeta"; //$NON-NLS-1$
	public static final String MACROS = BASE + "macros"; //$NON-NLS-1$
	public static final String SERVER_ADDR = BASE + "serverAddr"; //$NON-NLS-1$
	public static final String SERVER_PORT = BASE + "serverPort"; //$NON-NLS-1$
	public static final String RESTRICT_CURRENT = BASE + "restrictToCurrentPatient"; //$NON-NLS-1$
	public static final String MAXIMUM_HITS=BASE+"maximumHitNumber"; //$NON-NLS-1$
	public static final String SHOW_INBOX = BASE + "showInbox"; //$NON-NLS-1$
	public static final String SHOW_OMNIVORE = BASE + "showOmnivore"; //$NON-NLS-1$
	public static final String SHOW_CONS = BASE + "showConsultation"; //$NON-NLS-1$
	public static final String COLUMN_WIDTHS = BASE + "columnWidths"; //$NON-NLS-1$
	public static final String OMNIVORE_MOVE = BASE + "omnivore_MoveFiles"; //$NON_NLS-1$
	public static final String OMNIVORE_EXCLUDE = BASE + "omnivore_excludeCat";//$NON-NLS-1$
	
	public static final String INBOX_NAME = Messages.Preferences_Inbox_Name;
	public static final String OMNIVORE_NAME = Messages.Preferences_Omnivore_Name;
	public static final String KONSULTATION_NAME = Messages.Preferences_Konsultation_Name;
	public static final String FLD_ID = "id"; //$NON-NLS-1$
	public static final String FLD_LUCINDA_DOCTYPE = "lucinda_doctype"; //$NON-NLS-1$
	public static final String FLD_LOCATION="loc"; //$NON-NLS-1$
	
	public static final String AQUIRE_ACTION_SCRIPT=BASE+"aquire_action"; //$NON-NLS-1$
	public static final String AQUIRE_ACTION_SCRIPTS=BASE+"aquire_actions"; //$NON-NLS-1$
	public static final String AQUIRE_ACTION_NAME="Aquire"; 
	
	public static final String DOCUMENT_STORE=BASE+"document_store"; //$NON-NLS-1$
	
	public static final String COMMON_DIRECTORY = BASE + "commonDir";  //$NON-NLS-1$
	public static final String USE_COMMON_DIRECTORY = BASE + "useCommonDir";  //$NON-NLS-1$
	public static final String DEFAULT_MAILSUBJECT = BASE + "mailsubject"; //$NON-NLS-1$
	public static final String DEFAULT_MAILBODY = BASE + "mailbody"; //$NON-NLS-1$
	

	 
	public List<String> getNetworks() {
		ArrayList<String> ret = new ArrayList<String>();
		for (String ip : NetTool.IPs) {
			if (ip.split(".").length == 4) { //$NON-NLS-1$
				ret.add(ip);
			}
		}
		return ret;
	}

	public static String get(final String key, final String def) {
		return cfg.get(key, def);
	}

	public static void set(final String key, final String value) {
		cfg.set(key, value);
	}

	public static void set(final String key, final boolean value) {
		set(key, Boolean.toString(value));
	}

	public static boolean is(final String key) {
		return Boolean.parseBoolean(get(key, Boolean.toString(false)));
	}
}

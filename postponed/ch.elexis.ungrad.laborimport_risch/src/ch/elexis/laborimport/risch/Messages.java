/**
 * Copyright (c) 2007-2016, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package ch.elexis.laborimport.risch;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.laborimport.risch.messages"; //$NON-NLS-1$
	public static String PreferencePage_DownloadDir;
	public static String PreferencePage_JMedTrasferJar;
	public static String PreferencePage_JMedTrasferJni;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}

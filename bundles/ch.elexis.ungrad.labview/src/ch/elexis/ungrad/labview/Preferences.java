/*******************************************************************************
 * Copyright (c) 2016-2026 by G. Weirich
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

import ch.elexis.ungrad.Config;

public class Preferences {
	public static final Config cfg = Config.getDefaultInstance();
	public static final String BASE = "ch.elexis.ungrad.labview.";
	public static final String MODE = BASE + "mode";
	// public static final String COLWIDTHS = BASE + "colwidths";
	public static final String CONDVIEW = BASE + "condView";
	public static final String FULLVIEW = BASE + "fullView";
	public static final String SMARTVIEW = BASE + "smartView";
	public static final String TEMPLATE = BASE + "template";
	public static final String EXCLUDE = BASE + "exclusions";
	
}

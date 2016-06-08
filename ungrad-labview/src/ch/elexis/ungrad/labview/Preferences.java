/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
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

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.io.Settings;

public class Preferences {
	public static final Settings cfg = CoreHub.localCfg;
	public static final String BASE = "ch.elexis.ungrad.labview.";
	public static final String MODE = BASE + "mode";
	public static final String COLWIDTHS = BASE + "colwidths";

}

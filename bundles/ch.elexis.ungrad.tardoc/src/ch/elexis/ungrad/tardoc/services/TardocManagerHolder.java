/*******************************************************************************
 * Copyright (c) 2025 by G. Weirich
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

package ch.elexis.ungrad.tardoc.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * Static holder for TardocManager service to make it accessible in Eclipse views
 * that don't support direct OSGi injection.
 * * We have to do quite a lot boilerplate coding, because ch.elexis.base.arzttarife.service.ArzttarifeModelServiceHolder
 * is not API accessible.
 * This is copilot's solution.
 */
@Component
public class TardocManagerHolder {
	
	private static TardocManager tardocManager;
	
	@Reference
	public void setTardocManager(TardocManager manager) {
		TardocManagerHolder.tardocManager = manager;
	}
	
	public void unsetTardocManager(TardocManager manager) {
		if (TardocManagerHolder.tardocManager == manager) {
			TardocManagerHolder.tardocManager = null;
		}
	}
	
	public static TardocManager get() {
		return tardocManager;
	}
}

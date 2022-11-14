/*******************************************************************************
 * Copyright (c) 2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.forms;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.ungrad.forms.model.Controller;

/**
 * The Activator checks if the plugins's category is already set up in the
 * document list, and adds it, if not.
 * 
 * @author gerry
 *
 */
public class Activator extends AbstractUIPlugin {
	public static final String DOC_CATEGORY = "Formular";
	public static final String KonsXRef = "ch.elexis.ungrad.Forms";
	private static Controller controller;
	private static Activator theInstance;

	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		super.start(context);
		String cats = CoreHub.globalCfg.get(Preferences.DOC_CATEGORY, "Alle,Allg.,AUF-Zeugnis,Rezept,Labor");
		if (!cats.contains(DOC_CATEGORY)) {
			cats += "," + DOC_CATEGORY;
			CoreHub.globalCfg.set(Preferences.DOC_CATEGORY, cats);
		}
		controller = new Controller();
	}

	public static Controller getController() {
		return controller;
	}
	

}

/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 *     G.Weirich - make preferred provider configurable
 ******************************************************************************/
package at.medevit.ch.artikelstamm.elexis.common.ui.cv;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.wb.swt.ResourceManager;

import ch.elexis.core.data.activator.CoreHub;

public class PreferedProviderSorterAction extends Action {

	private ArtikelstammFlatDataLoader afdl;

	public static final String CFG_PREFER_PROVIDER = "artikelstammPreferProvider";

	public PreferedProviderSorterAction(ArtikelstammFlatDataLoader afdl) {
		this.afdl = afdl;
	}

	@Override
	public String getText() {
		return "Pref";
	}

	@Override
	public String getToolTipText() {
		return "Artikel des gewünschten Herstellers bevorzugen (werden zuoberst angezeigt)";
	}

	@Override
	public int getStyle() {
		return Action.AS_CHECK_BOX;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ResourceManager.getPluginImageDescriptor("at.medevit.ch.artikelstamm.ui", "/rsc/icons/heart.png");
	}

	@Override
	public void run() {
		CoreHub.globalCfg.set(CFG_PREFER_PROVIDER, isChecked());
		afdl.setUsePreferredProviderSorter(isChecked());
	}
}

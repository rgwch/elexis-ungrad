/*******************************************************************************
 * Copyright (c) 2022 by G. Weirich
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

package ch.elexis.ungrad.textplugin.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	IPreferenceStore store;
	
	public PreferencePage() {
		super(GRID);
		store=new SettingsPreferenceStore(CoreHub.localCfg);
	}
	@Override
	public void init(IWorkbench arg0) {
	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.TEMPLATE_DIR, "Vorlagenverzeichnis", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.DOCUMENT_BASE,"Dokumentenverzeichnis", getFieldEditorParent()));
	
	}
	
	protected void performApply() {
		CoreHub.localCfg.flush();
	}

}

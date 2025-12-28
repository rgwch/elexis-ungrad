/*******************************************************************************
 * Copyright (c) 2007-2024, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.buchhaltung.kassenbuch;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;

public class KassenbuchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public KassenbuchPreferencePage() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription(Messages.PreferencePage_Description);
	}

	@Override
	protected void createFieldEditors() {
		addField(new FileFieldEditor(PreferenceConstants.HTML_TEMPLATE, 
				Messages.PreferencePage_HTMLTemplate, 
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to initialize
	}

	@Override
	protected void performApply() {
		super.performApply();
		CoreHub.localCfg.flush();
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		CoreHub.localCfg.flush();
		return result;
	}
}

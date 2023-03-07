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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.ungrad.lucinda.Preferences;

public class LucindaPrefs extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public LucindaPrefs() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription("Lucinda Client"); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(Preferences.SERVER_ADDR, Messages.LucindaPrefs_serverAddress,
				getFieldEditorParent()));
		addField(new StringFieldEditor(Preferences.SERVER_PORT, Messages.LucindaPrefs_serverPort,
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(Preferences.MAXIMUM_HITS, "maximum hit count", getFieldEditorParent()));
		// addField(new FileFieldEditor(Preferences.AQUIRE_ACTION_SCRIPT, "Aquire script", getFieldEditorParent()));
		addField(new MultilineFieldEditor(Preferences.AQUIRE_ACTION_SCRIPTS, "Einlese-Scripts", getFieldEditorParent()));
		/* addField(new StringFieldEditor(Preferences.DOCUMENT_STORE, Messages.LucindaPrefs_documentStore,
				getFieldEditorParent())); */
		addField(new MultilineFieldEditor(Preferences.EXCLUDEMETA, Messages.LucindaPrefs_exclude_Metadata, 5, 0, true,
				getFieldEditorParent()));
		// addField(new MultilineFieldEditor(Preferences.MACROS, "Makros für das
		// Suchfeld", 5,0,true,getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.COMMON_DIRECTORY, "Ungrad-Dokumenten-Verzeichnis verwenden (Neustart nötig)", getFieldEditorParent()));

	}

}

/*******************************************************************************
 * Copyright (c) 2023, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.inbox.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.ungrad.inbox.model.PreferenceConstants;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public Preferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription("Ungrad Inbox");
	}

	@Override
	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub

	}

	private String[][] methods = { { "Nein", "none" }, { "IMAP", "imap" }, { "Mbox", "mbox" } };

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.BASEDIR, "Verzeichnis", getFieldEditorParent()));
		addField(
				new MultilineFieldEditor(PreferenceConstants.WHITELIST, "Dokumenten-Absender", getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(PreferenceConstants.MAILMODE, "Emails einlesen", 3, methods,
				getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.MBOX, "MBox-Datei", getFieldEditorParent()));
	}

}

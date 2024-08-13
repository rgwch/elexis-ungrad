/*******************************************************************************
 * Copyright (c) 2022-2024 by G. Weirich
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

package ch.elexis.ungrad.common.ui;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.PasswordFieldEditor;
import ch.elexis.ungrad.PreferenceConstants;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	static String[][] conn = { { "Unverschlüsselt", "plain" }, { "TLS", "tls" }, { "SSL", "ssl" } };

	public Preferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription("Ungrad Gemeinsames");

	}

	@Override
	public void init(IWorkbench arg0) {

	}

	@Override
	protected void createFieldEditors() {
		addField(
				new DirectoryFieldEditor(PreferenceConstants.DOCBASE, "Dokumentenverzeichnis", getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(PreferenceConstants.SMTP_SECURITY, "SMTP-Verbindung", 3, conn,
				getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.MAIL_SENDER, "Mail-Absender", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SMTP_HOST, "SMTP Server", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SMTP_PORT, "SMTP Port", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SMTP_USER, "SMTP User", getFieldEditorParent()));
		addField(new PasswordFieldEditor(PreferenceConstants.SMTP_PWD, "SMTP Passwort", getFieldEditorParent()));

		// addField(new RadioGroupFieldEditor(PreferenceConstants.IMAP_SECURITY,
		// "IMAP-Verbindung", 3 , conn , getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.IMAP_HOST, "IMAP Server", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.IMAP_PORT, "IMAP Port",getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.IMAP_USER, "IMAP User", getFieldEditorParent()));
		addField(new PasswordFieldEditor(PreferenceConstants.IMAP_PWD, "IMAP Passwort", getFieldEditorParent()));
	}

	protected void performApply() {
		CoreHub.localCfg.flush();
	}

}

/*******************************************************************************
 * Copyright (c) 2022-2025 by G. Weirich
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

import org.eclipse.jface.preference.BooleanFieldEditor;
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
	static String[][] conn = { 
		{ ch.elexis.ungrad.common.Messages.Preferences_ConnectionPlain, "plain" }, 
		{ ch.elexis.ungrad.common.Messages.Preferences_ConnectionTLS, "tls" }, 
		{ ch.elexis.ungrad.common.Messages.Preferences_ConnectionSSL, "ssl" } 
	};

	public Preferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription(ch.elexis.ungrad.common.Messages.Preferences_Description);

	}

	@Override
	public void init(IWorkbench arg0) {

	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.DOCBASE, 
				ch.elexis.ungrad.common.Messages.Preferences_DocDirectory, getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(PreferenceConstants.SMTP_SECURITY, 
				ch.elexis.ungrad.common.Messages.Preferences_SMTPConnection, 3, conn,
				getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.MAIL_SENDER, 
				ch.elexis.ungrad.common.Messages.Preferences_MailSender, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SMTP_HOST, 
				ch.elexis.ungrad.common.Messages.Preferences_SMTPServer, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SMTP_PORT, 
				ch.elexis.ungrad.common.Messages.Preferences_SMTPPort, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SMTP_USER, 
				ch.elexis.ungrad.common.Messages.Preferences_SMTPUser, getFieldEditorParent()));
		addField(new PasswordFieldEditor(PreferenceConstants.SMTP_PWD, 
				ch.elexis.ungrad.common.Messages.Preferences_SMTPPassword, getFieldEditorParent()));

		// addField(new RadioGroupFieldEditor(PreferenceConstants.IMAP_SECURITY,
		// "IMAP-Verbindung", 3 , conn , getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.IMAP_HOST, 
				ch.elexis.ungrad.common.Messages.Preferences_IMAPServer, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.IMAP_PORT, 
				ch.elexis.ungrad.common.Messages.Preferences_IMAPPort,getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.IMAP_USER, 
				ch.elexis.ungrad.common.Messages.Preferences_IMAPUser, getFieldEditorParent()));
		addField(new PasswordFieldEditor(PreferenceConstants.IMAP_PWD, 
				ch.elexis.ungrad.common.Messages.Preferences_IMAPPassword, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.USE_AI, 
				ch.elexis.ungrad.common.Messages.Preferences_UseAI, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.AI_URL, 
				ch.elexis.ungrad.common.Messages.Preferences_AIUrl, getFieldEditorParent()));
		
	}

	protected void performApply() {
		CoreHub.localCfg.flush();
	}

}

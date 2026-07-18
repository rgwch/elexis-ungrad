/*******************************************************************************
 * Copyright (c) 2022-2026, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.forms.ui;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.ungrad.forms.model.PreferenceConstants;

/**
 * @author gerry
 *
 */
public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public Preferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription(Messages.Preferences_Description);
		CoreHub.localCfg.set(PreferenceConstants.SIGNATURE_X, CoreHub.localCfg.get(PreferenceConstants.SIGNATURE_X, "10"));
		CoreHub.localCfg.set(PreferenceConstants.SIGNATURE_Y, CoreHub.localCfg.get(PreferenceConstants.SIGNATURE_Y, "10"));
	}

	@Override
	public void init(IWorkbench arg0) {

	}

	@Override
	protected void createFieldEditors() {
		addField(
				new DirectoryFieldEditor(PreferenceConstants.TEMPLATES, Messages.Preferences_Templates, getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.PUG, Messages.Preferences_PugCompiler, getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.PDF_VIEWER, Messages.Preferences_PDFViewer, getFieldEditorParent()));
		addField(new MultilineFieldEditor(PreferenceConstants.MAIL_BODY, Messages.Preferences_MailBody, getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.SIGNATURE, Messages.Preferences_Signature, getFieldEditorParent()));
		
		StringFieldEditor signatureX = new StringFieldEditor(PreferenceConstants.SIGNATURE_X, Messages.Preferences_SignatureX, 5, getFieldEditorParent());
		signatureX.setTextLimit(5);
		signatureX.setEmptyStringAllowed(false);
		addField(signatureX);
		
		StringFieldEditor signatureY = new StringFieldEditor(PreferenceConstants.SIGNATURE_Y, Messages.Preferences_SignatureY, 5, getFieldEditorParent());
		signatureY.setTextLimit(5);
		signatureY.setEmptyStringAllowed(false);
		addField(signatureY);

	}

	protected void performApply() {
		CoreHub.localCfg.flush();
	}

}
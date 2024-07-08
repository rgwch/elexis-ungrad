/*******************************************************************************
 * Copyright (c) 2022-2024, G. Weirich and Elexis
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
		setDescription("Ungrad Forms");
	}

	@Override
	public void init(IWorkbench arg0) {

	}

	@Override
	protected void createFieldEditors() {
		addField(
				new DirectoryFieldEditor(PreferenceConstants.TEMPLATES, "Vorlagenverzeichnis", getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.PUG, "Pug-Compiler", getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.PDF_VIEWER, "PDF Viewer", getFieldEditorParent()));
		addField(new MultilineFieldEditor(PreferenceConstants.MAIL_BODY, "Mail-Standardtext", getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.SIGNATURE, "Signatur", getFieldEditorParent()));

	}

	protected void performApply() {
		CoreHub.localCfg.flush();
	}

}

package ch.elexis.ungrad.common.ui;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.PasswordFieldEditor;
import ch.elexis.ungrad.PreferenceConstants;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

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
		addField(new StringFieldEditor(PreferenceConstants.SMTP_HOST, "SMTP Server", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SMTP_PORT, "SMTP Port", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SMTP_USER, "SMTP User", getFieldEditorParent()));
		addField(new PasswordFieldEditor(PreferenceConstants.SMTP_PWD, "SMTP Passwort", getFieldEditorParent()));

	}
	
	protected void performApply() {
		CoreHub.localCfg.flush();
	}


}
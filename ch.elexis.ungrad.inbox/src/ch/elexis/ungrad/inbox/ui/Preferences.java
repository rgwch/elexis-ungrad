package ch.elexis.ungrad.inbox.ui;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.ungrad.inbox.model.PreferenceConstants;


public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{

	public Preferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription("Ungrad Inbox");
	}
	@Override
	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.BASEDIR, "Verzeichnis", getFieldEditorParent()));
		
	}

}

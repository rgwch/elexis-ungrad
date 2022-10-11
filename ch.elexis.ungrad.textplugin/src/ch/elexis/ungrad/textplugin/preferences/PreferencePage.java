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
	DirectoryFieldEditor dfTemplateBase;
	
	public PreferencePage() {
		super(GRID);
		store=new SettingsPreferenceStore(CoreHub.localCfg);
	}
	@Override
	public void init(IWorkbench arg0) {
	}

	@Override
	protected void createFieldEditors() {
		dfTemplateBase=new DirectoryFieldEditor(PreferenceConstants.TEMPLATE_DIR, "Vorlagenverzeichnis", getFieldEditorParent());
		addField(dfTemplateBase);
	
	}
	
	protected void performApply() {
		CoreHub.localCfg.flush();
	}

}

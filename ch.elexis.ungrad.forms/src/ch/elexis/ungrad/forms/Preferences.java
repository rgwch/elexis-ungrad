/**
 * 
 */
package ch.elexis.ungrad.forms;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;

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
		addField(new DirectoryFieldEditor(PreferenceConstants.TEMPLATES, "Vorlagenverzeichnis", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.OUTPUT,"Dokumentenverzeichnis", getFieldEditorParent()));
	
	}
	protected void performApply() {
		CoreHub.localCfg.flush();
	}


}

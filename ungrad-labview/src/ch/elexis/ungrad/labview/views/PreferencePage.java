package ch.elexis.ungrad.labview.views;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.FileSelectorField;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.ungrad.labview.Preferences;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public PreferencePage(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription("Ungrad Labview");
		
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new FileFieldEditor(Preferences.TEMPLATE, "Vorlage für HTML Export",
			getFieldEditorParent()));
		addField(new MultilineFieldEditor(Preferences.EXCLUDE, "Bei Ausgabe unterdrücken", 5, 0,
			true, getFieldEditorParent()));
	}
	
	@Override
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
}

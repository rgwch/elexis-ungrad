package ch.elexis.ungrad.modifier;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.ungrad.Config;
	
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Config.getDefaultInstance());
		setDescription("Ungrad Inbox");

	}


	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createFieldEditors() {
		addField(new FileFieldEditor(PreferenceConstants.EXEC, "Programm", getFieldEditorParent()));
		
	}
	

}

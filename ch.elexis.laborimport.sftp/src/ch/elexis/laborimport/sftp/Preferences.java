/**
 * (c) 2008-2016 by G. Weirich
 * All rights reserved
 * 
 */
package ch.elexis.laborimport.sftp;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String SFTP_LABNAME ="LABORNAME";
	public static final String SFTP_URL = "labor/sftp/ftp_url";
	public static final String SFTP_PORT = "labor/sftp/ftp_port";
	public static final String SFTP_USER = "labor/sftp/username";
	public static final String SFTP_PWD = "labor/sftp/password";
	public static final String SFTP_DELETE = "labor/sftp/deletereceived";
	public static final String DL_DIR = "labor/sftp/downloaddir";
	
	public Preferences(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		getPreferenceStore().setDefault(SFTP_URL, "62.202.19.130");
		getPreferenceStore().setDefault(SFTP_PORT, "22");
		getPreferenceStore().setDefault(SFTP_DELETE, true);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new StringFieldEditor(SFTP_LABNAME,"Name des Labors im HL-7",getFieldEditorParent()));
		addField(new StringFieldEditor(SFTP_URL, "Adresse f\u00FCr FTP", getFieldEditorParent()));
		addField(new StringFieldEditor(SFTP_PORT, "Port f\u00FCr FTP", getFieldEditorParent()));
		addField(new StringFieldEditor(SFTP_USER, "Username", getFieldEditorParent()));
		addField(new StringFieldEditor(SFTP_PWD, "Passwort", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(DL_DIR, "Download Verzeichnis", getFieldEditorParent()));
		addField(new BooleanFieldEditor(SFTP_DELETE, "Nach Download vom Server l\u00F6schen",
			getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
}

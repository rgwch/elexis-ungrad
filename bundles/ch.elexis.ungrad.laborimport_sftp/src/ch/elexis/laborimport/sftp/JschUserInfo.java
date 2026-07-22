/**
 * (c) 2008-2016 by G. Weirich
 * All rights reserved
 * 
 */
package ch.elexis.laborimport.sftp;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;

import com.jcraft.jsch.UserInfo;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.Config;

/**
 * Override UserInfo to supply our info from Configuration
 * @author gerry
 *
 */
public class JschUserInfo implements UserInfo {
	
	Config cfg = Config.getDefaultInstance();
	/**
	 * Get the passphrase
	 */
	public String getPassphrase(){
		return cfg.getString(Preferences.SFTP_PWD);
	}
	
	/**
	 * Get the Password - Same as passphrase here
	 */
	public String getPassword(){
		return cfg.getString(Preferences.SFTP_PWD);
	}
	
	/**
	 * Prompt for a passphrase and store it
	 */
	public boolean promptPassphrase(String message){
		InputDialog input =
			new InputDialog(UiDesk.getTopShell(), "Passworteingabe", message, "", null);
		if (input.open() == Dialog.OK) {
			cfg.setValue(Preferences.SFTP_PWD, input.getValue());
			return true;
		}
		return false;
	}
	
	
	/**
	 * Prompt for a Password - same as passphrase
	 * @param message prompt for the message box
	 */
	public boolean promptPassword(String message){
		return promptPassphrase(message);
	}
	
	/**
	 * Ask the user something 
	 * @param message: prompt of the message box
	 */
	public boolean promptYesNo(String message){
		return SWTHelper.askYesNo("Laborimport "+cfg.getString(Preferences.SFTP_LABNAME, "unknown"), message);
	}
	
	/**
	 * Show a message
	 * @param message prompt for the message box
	 */
	public void showMessage(String message){
		SWTHelper.showInfo("Laborimport "+cfg.getString(Preferences.SFTP_LABNAME, "unknown"), message);
		
	}
	
}

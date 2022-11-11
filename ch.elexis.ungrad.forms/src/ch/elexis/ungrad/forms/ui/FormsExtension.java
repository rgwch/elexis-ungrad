/*******************************************************************************
 * Copyright (c) 2022, G. Weirich and Elexis
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.program.Program;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.text.IRichTextDisplay;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.Brief;
import ch.elexis.ungrad.forms.Activator;
import ch.rgw.tools.ExHandler;

/**
 * An IKonsExtension fpr Ungrad Forms: Will create entries in Encounter-Entries for created documents
 * @author gerry
 *
 */
public class FormsExtension implements IKonsExtension {
	
	@Override
	public void setInitializationData(IConfigurationElement arg0, String arg1, Object arg2)
		throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String connect(IRichTextDisplay tf){
		return Activator.KonsXRef;
	}
	
	@Override
	public boolean doLayout(StyleRange styleRange, String provider, String id){
		styleRange.background = UiDesk.getColor(UiDesk.COL_GREEN);
		styleRange.foreground = UiDesk.getColor(UiDesk.COL_BLACK);
		return true;
	}
	
	@Override
	public boolean doXRef(String refProvider, String refID){
		try {
			Brief brief = Brief.load(refID);
			if (brief.isValid()) {
				File temp = File.createTempFile("letter_", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$
				temp.deleteOnExit();
				try (FileOutputStream fos = new FileOutputStream(temp)) {
					fos.write(brief.loadBinary());
				}
				Program.launch(temp.getAbsolutePath());
			}
		} catch (IOException e) {
			ExHandler.handle(e);
			SWTHelper.alert(Messages.BriefAuswahlErrorHeading, //$NON-NLS-1$
				Messages.BriefAuswahlCouldNotLoadText); //$NON-NLS-1$
		}
		return false;
	}
	
	@Override
	public IAction[] getActions(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void insert(Object o, int pos){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeXRef(String refProvider, String refID){
		Brief brief=Brief.load(refID);
		if(brief.isValid()) {
			brief.delete();
		}
		
	}
	
}

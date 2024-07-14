/*******************************************************************************
 * Copyright (c) 2016-2020 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/

package ch.elexis.ungrad.lucinda.omnivore;

import static ch.elexis.ungrad.lucinda.Preferences.INCLUDE_OMNI;
import static ch.elexis.ungrad.lucinda.Preferences.SHOW_OMNIVORE;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.ungrad.lucinda.IDocumentHandler;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.controller.Controller;
import ch.elexis.ungrad.lucinda.view.Messages;
/**
 * A DocumentHandler for Omnivore Documents
 * @author gerry
 *
 */
public class DocumentHandler implements IDocumentHandler {
	
	final private OmnivoreIndexer indexer = new OmnivoreIndexer();
	private ImageDescriptor icon;
	
	public DocumentHandler(){
		icon =
			AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis.ungrad.docmgr-lucinda", "icons/fressen.gif"); //$NON-NLS-1$
			
	}
	
	@Override
	public IAction getSyncAction(final Controller controller){
		IAction indexOmnivoreAction = new RestrictedAction(AccessControlDefaults.DOCUMENT_CREATE,
			Messages.GlobalView_omnivoreImport_Name, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_omnivoreImport_tooltip);
				setImageDescriptor(Images.IMG_SYNC.getImageDescriptor());
			}
			
			@Override
			public void doRun(){
				if (this.isChecked()) {
					ImportSettings dlg=new ImportSettings(Hub.getActiveShell());
					dlg.create();
					if(dlg.open()==Window.OK){
						indexer.start(controller);
					}else{
						this.setChecked(false);
						indexer.setActive(false);
					}
				} else {
					indexer.setActive(false);
				}
				
				Preferences.set(INCLUDE_OMNI, isChecked());
			}
		};
		if (Preferences.is(INCLUDE_OMNI)) {
			indexer.start(controller);
			indexOmnivoreAction.setChecked(true);
			
		}
		return indexOmnivoreAction;
	}
	
	@Override
	public IAction getFilterAction(final Controller controller){
		IAction showOmnivoreAction =
			new Action(Messages.GlobalView_filterOmni_name, Action.AS_CHECK_BOX) {
				{
					setToolTipText(Messages.GlobalView_filterOmni_tooltip);
					setImageDescriptor(icon);
					
				}
				
				@Override
				public void run(){
					controller.toggleDoctypeFilter(isChecked(), Preferences.OMNIVORE_NAME);
					Preferences.set(SHOW_OMNIVORE, isChecked());
				}
				
			};
		showOmnivoreAction.setChecked(Preferences.is(SHOW_OMNIVORE));
		return showOmnivoreAction;
	}
	
}

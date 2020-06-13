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

package ch.elexis.ungrad.docmgr.lucinda.kons;

import static ch.elexis.ungrad.lucinda.Preferences.INCLUDE_KONS;
import static ch.elexis.ungrad.lucinda.Preferences.SHOW_CONS;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.ungrad.lucinda.IDocumentHandler;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.controller.Controller;
import ch.elexis.ungrad.lucinda.view.Messages;

public class DocumentHandler implements IDocumentHandler {
	//ch.elexis.core.ui.icons/icons/16x16/book_open_view.png
	private ConsultationIndexer indexer = new ConsultationIndexer();
	private ImageDescriptor icon;
	
	public DocumentHandler(){
		icon = AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis.core.ui.icons", //$NON-NLS-1$
			"/icons/16x16/book_open_view.png");
			
	}
	
	@Override
	public IAction getSyncAction(Controller controller){
		IAction indexKonsAction = new RestrictedAction(AccessControlDefaults.DOCUMENT_CREATE,
			Messages.GlobalView_synckons_Name, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_synckons_tooltip);
				setImageDescriptor(Images.IMG_GEAR.getImageDescriptor());
			}
			
			@Override
			public void doRun(){
				if (this.isChecked()) {
					indexer.start(controller);
				} else {
					indexer.setActive(false);
				}
				Preferences.set(INCLUDE_KONS, isChecked());
			}
		};
		if (Preferences.is(INCLUDE_KONS)) {
			indexer.start(controller);
			indexKonsAction.setChecked(true);
		}
		
		return indexKonsAction;
		
	}
	
	@Override
	public IAction getFilterAction(Controller controller){
		IAction showConsAction =
			new Action(Messages.GlobalView_filterKons_name, Action.AS_CHECK_BOX) {
				{
					setToolTipText(Messages.GlobalView_filterKons_tooltip);
					setImageDescriptor(icon);
				}
				
				@Override
				public void run(){
					controller.toggleDoctypeFilter(isChecked(), Preferences.KONSULTATION_NAME);
					Preferences.set(SHOW_CONS, isChecked());
				}
			};
		showConsAction.setChecked(Preferences.is(SHOW_CONS));
		return showConsAction;
	}
	
}

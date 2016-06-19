package ch.elexis.ungrad.lucinda.omnivore;

import static ch.elexis.ungrad.lucinda.Preferences.INCLUDE_OMNI;
import static ch.elexis.ungrad.lucinda.Preferences.SHOW_OMNIVORE;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.ungrad.lucinda.Activator;
import ch.elexis.ungrad.lucinda.IDocumentHandler;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.controller.Controller;
import ch.elexis.ungrad.lucinda.view.Messages;

public class DocumentHandler implements IDocumentHandler {

	@Override
	public IAction getSyncAction(final Controller controller) {
		IAction indexOmnivoreAction = new RestrictedAction(AccessControlDefaults.DOCUMENT_CREATE,
				Messages.GlobalView_omnivoreImport_Name, Action.AS_CHECK_BOX) {
				{
					setToolTipText(Messages.GlobalView_omnivoreImport_tooltip);
					setImageDescriptor(Images.IMG_DATABASE.getImageDescriptor());
				}
				
				@Override
				public void doRun(){
					//Activator.getDefault().syncOmnivore(this.isChecked());
					Preferences.set(INCLUDE_OMNI, isChecked());
				}
			};
			indexOmnivoreAction.setChecked(Preferences.is(INCLUDE_OMNI));
			return indexOmnivoreAction;
	}

	@Override
	public IAction getFilterAction(final Controller controller) {
		IAction showOmnivoreAction = new Action(Messages.GlobalView_filterOmni_name, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_filterOmni_tooltip);
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

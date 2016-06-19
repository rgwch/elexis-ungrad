package ch.elexis.ungrad.docmgr.lucinda.kons;

import static ch.elexis.ungrad.lucinda.Preferences.INCLUDE_KONS;
import static ch.elexis.ungrad.lucinda.Preferences.SHOW_CONS;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.ungrad.lucinda.IDocumentHandler;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.controller.Controller;
import ch.elexis.ungrad.lucinda.view.Messages;

public class DocumentHandler implements IDocumentHandler {

	@Override
	public IAction getSyncAction(Controller controller) {
		IAction indexKonsAction = new RestrictedAction(AccessControlDefaults.DOCUMENT_CREATE,
				Messages.GlobalView_synckons_Name, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_synckons_tooltip);
				setImageDescriptor(Images.IMG_GEAR.getImageDescriptor());
			}

			@Override
			public void doRun() {
				// Activator.getDefault().syncKons(this.isChecked());
				Preferences.set(INCLUDE_KONS, isChecked());
			}
		};
		indexKonsAction.setChecked(Preferences.is(INCLUDE_KONS));
		return indexKonsAction;

	}

	@Override
	public IAction getFilterAction(Controller controller) {
		IAction showConsAction = new Action(Messages.GlobalView_filterKons_name, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalView_filterKons_tooltip);
			}

			@Override
			public void run() {
				controller.toggleDoctypeFilter(isChecked(), Preferences.KONSULTATION_NAME);
				Preferences.set(SHOW_CONS, isChecked());
			}
		};
		showConsAction.setChecked(Preferences.is(SHOW_CONS));
		return showConsAction;
	}

}

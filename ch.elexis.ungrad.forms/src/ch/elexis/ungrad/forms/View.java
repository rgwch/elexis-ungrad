/**
 * 
 */
package ch.elexis.ungrad.forms;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.data.Patient;

/**
 * @author gerry
 *
 */
public class View extends ViewPart implements IActivationListener{
	private Controller controller;
	
	private final ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
			ElexisEvent.EVENT_SELECTED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			controller.changePatient((Patient) ev.getObject());
		}

	};

	public View() {
		controller=new Controller();
	}
	@Override
	public void createPartControl(Composite arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void activation(boolean mode) {
		
	}

	@Override
	public void visible(boolean mode) {
		if(mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
		}else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
		}
		
	}

}

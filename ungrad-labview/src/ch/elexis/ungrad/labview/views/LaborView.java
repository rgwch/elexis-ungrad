package ch.elexis.ungrad.labview.views;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.controller.Controller;

public class LaborView extends ViewPart implements IActivationListener{
	Controller controller=new Controller();
	NatTable nat;
	
	private final ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
			ElexisEvent.EVENT_SELECTED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			controller.setPatient((Patient) ev.getObject());
		}
	

	};
	@Override
	public void createPartControl(Composite parent) {
		nat=new NatTable(parent,controller.getBaseLayer());
		nat.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	public void visible(final boolean mode) {
		controller.setPatient(ElexisEventDispatcher.getSelectedPatient());
		nat.doCommand(new VisualRefreshCommand());
		nat.refresh();
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
			//save(COLUMN_WIDTHS,controller.getColumnWidths());
		}
	}

	@Override
	public void activation(boolean mode) {
		// TODO Auto-generated method stub
		
	}

}

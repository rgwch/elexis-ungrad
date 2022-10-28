/**
 * 
 */
package ch.elexis.ungrad.forms;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Patient;

/**
 * @author gerry
 *
 */
public class View extends ViewPart implements IActivationListener{
	private Controller controller;
	private TableViewer tv;
	private Action doubleClickAction;
	
	private final ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
			ElexisEvent.EVENT_SELECTED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			// controller.changePatient((Patient) ev.getObject());
			tv.setInput(ev.getObject());
		}

	};

	public View() {
		controller=new Controller();
	}
	@Override
	public void createPartControl(Composite parent) {
		visible(true);
		tv=new TableViewer(parent);
		tv.setContentProvider(controller);
		tv.setLabelProvider(controller);
		tv.setComparator(new ViewerComparator());
		makeActions();
		contributeToActionBars();
		tv.setInput(ElexisEventDispatcher.getSelectedPatient());
		GlobalEventDispatcher.addActivationListener(this, this);
		
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		IMenuManager menu = bars.getMenuManager();
		IToolBarManager toolbar = bars.getToolBarManager();
		toolbar.add(doubleClickAction);
			//menu.add();

	}

	private void makeActions() {
		doubleClickAction=new Action("Laden") {
			{
				setToolTipText("Ein Dokument mit diesem Formular erstellen");
				setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
				setText("Neu");
			}
			@Override
			public void run() {
				
			}
		};
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

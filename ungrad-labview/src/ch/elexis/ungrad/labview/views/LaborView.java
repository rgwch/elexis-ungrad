/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
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
package ch.elexis.ungrad.labview.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.controller.Controller;

public class LaborView extends ViewPart implements IActivationListener {
	Controller controller = new Controller(this);
	Log log = Log.get("LaborView");

	private final ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
			ElexisEvent.EVENT_SELECTED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			try {
				controller.setPatient((Patient) ev.getObject());
			} catch (ElexisException e) {
				log.log(e, "error loading patient data", Log.ERRORS);
			}
		}

	};

	@Override
	public void createPartControl(Composite parent) {
		Control ctl = controller.createPartControl(parent);
		ctl.setLayoutData(SWTHelper.getFillGridData());
		GlobalEventDispatcher.addActivationListener(this, this);

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void visible(final boolean mode) {
		try {
			controller.setPatient(ElexisEventDispatcher.getSelectedPatient());
		} catch (ElexisException e) {
			log.log(e, "error loading patient data", Log.ERRORS);
		}
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
			controller.saveState();
		}
	}

	@Override
	public void activation(boolean mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		controller.dispose();
	}

}

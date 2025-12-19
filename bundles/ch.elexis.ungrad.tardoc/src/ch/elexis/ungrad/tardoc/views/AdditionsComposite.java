/*******************************************************************************
 * Copyright (c) 2025 by G. Weirich
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

package ch.elexis.ungrad.tardoc.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;

/**
 * Composite to hold additional content (billing positions, diagnoses, etc.)
 * Uses StackLayout to switch between different views
 */
public class AdditionsComposite extends Composite {

	private StackLayout stackLayout;
	private BillingPositionsComposite billingPositionsComposite;
	private DiagnosesComposite diagnosesComposite;
	private boolean showingBillingPositions = true;

	public AdditionsComposite(Composite parent, int style, TardocKonsView view, IMemento memento) {
		super(parent, style);
		
		stackLayout = new StackLayout();
		setLayout(stackLayout);
		
		// Create billing positions composite
		billingPositionsComposite = new BillingPositionsComposite(this, SWT.NONE, view);
		
		// Create diagnoses composite with memento for state persistence
		diagnosesComposite = new DiagnosesComposite(view.getSite().getPage(), this, SWT.NONE, memento);
		
		// Inject E4 context to enable event handling
		ch.elexis.core.ui.e4.util.CoreUiUtil.injectServices(billingPositionsComposite);
		
		// Initially show billing positions
		stackLayout.topControl = billingPositionsComposite;
		layout();
	}

	/**
	 * Toggle between billing positions and diagnoses view
	 */
	public void toggleView() {
		showingBillingPositions = !showingBillingPositions;
		
		if (showingBillingPositions) {
			stackLayout.topControl = billingPositionsComposite;
		} else {
			stackLayout.topControl = diagnosesComposite;
		}
		
		layout();
	}

	/**
	 * @return true if currently showing billing positions, false if showing diagnoses
	 */
	public boolean isShowingBillingPositions() {
		return showingBillingPositions;
	}

	/**
	 * @return the billing positions composite
	 */
	public BillingPositionsComposite getBillingPositionsComposite() {
		return billingPositionsComposite;
	}

	/**
	 * @return the diagnoses composite
	 */
	public DiagnosesComposite getDiagnosesComposite() {
		return diagnosesComposite;
	}
	
	/**
	 * Saves the current state to the memento.
	 * 
	 * @param memento the memento to save to
	 */
	public void saveState(IMemento memento) {
		if (diagnosesComposite != null && !diagnosesComposite.isDisposed()) {
			diagnosesComposite.saveState(memento);
		}
	}
}

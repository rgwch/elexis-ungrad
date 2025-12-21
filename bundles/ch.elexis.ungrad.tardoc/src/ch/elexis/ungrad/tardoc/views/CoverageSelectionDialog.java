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
* Substantial contributions:  Copilot (c) 2024 GitHub, Inc. using Claude Sonnet 4.5
*********************************************************************************/

package ch.elexis.ungrad.tardoc.views;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.util.CoverageComparator;
import ch.elexis.core.ui.views.provider.CoverageColorLabelProvider;

/**
 * Dialog for selecting a coverage (case) for a patient.
 */
public class CoverageSelectionDialog extends Dialog {
	private IPatient patient;
	private ICoverage selectedCoverage;
	private ICoverage currentCoverage;
	private TableViewer tableViewer;

	public CoverageSelectionDialog(Shell parentShell, IPatient patient, ICoverage currentCoverage) {
		super(parentShell);
		this.patient = patient;
		this.currentCoverage = currentCoverage;
		this.selectedCoverage = currentCoverage;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select Case (Fall)");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		container.setLayout(layout);

		Label label = new Label(container, SWT.NONE);
		label.setText("Select a case for the current consultation:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Create table viewer
		tableViewer = new TableViewer(container, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 200;
		gd.widthHint = 400;
		tableViewer.getTable().setLayoutData(gd);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new CoverageColorLabelProvider());

		// Load coverages
		if (patient != null) {
			List<ICoverage> coverages = patient.getCoverages();
			Collections.sort(coverages, new CoverageComparator());
			tableViewer.setInput(coverages);

			// Select current coverage if available
			if (currentCoverage != null) {
				tableViewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(currentCoverage));
			}
		}

		// Add selection listener
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					selectedCoverage = (ICoverage) selection.getFirstElement();
				}
			}
		});

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public ICoverage getSelectedCoverage() {
		return selectedCoverage;
	}
}

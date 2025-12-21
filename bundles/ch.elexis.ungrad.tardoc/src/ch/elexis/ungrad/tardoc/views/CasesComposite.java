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

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.ungrad.tardoc.Messages;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.Result;

/**
 * Composite for displaying and selecting cases (coverages) in the Tardoc consultation view.
 */
public class CasesComposite extends Composite {
	private TardocKonsView tkv;
	private Hyperlink coverageLink;
	private ICoverage currentCoverage;
	private IPatient currentPatient;

	public CasesComposite(Composite parent, TardocKonsView view) {
		super(parent, SWT.NONE);
		this.tkv = view;

		// Set layout for this composite
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 5;
		layout.marginHeight = 0;
		setLayout(layout);

		// Create hyperlink
		coverageLink = new Hyperlink(this, SWT.NONE);
		coverageLink.setText(Messages.CasesComposite_NoCase_Selected);
		coverageLink.setUnderlined(true);
		coverageLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Add click listener to show dialog
		coverageLink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				openCoverageSelectionDialog();
			}
		});
	}

	private void openCoverageSelectionDialog() {
		if (currentPatient == null) {
			return;
		}

		CoverageSelectionDialog dialog = new CoverageSelectionDialog(getShell(), currentPatient, currentCoverage);

		if (dialog.open() == Window.OK) {
			ICoverage selectedCoverage = dialog.getSelectedCoverage();
			if (selectedCoverage != null && !selectedCoverage.equals(currentCoverage)) {
				handleCoverageChange(selectedCoverage);
			}
		}
	}

	private void handleCoverageChange(ICoverage changeToCoverage) {
		ICoverage actCoverage = currentCoverage;
		String fallLabel = actCoverage != null ? actCoverage.getLabel() : "Current Case NOT found!!";

		if (!changeToCoverage.isOpen()) {
			SWTHelper.alert(ch.elexis.core.ui.views.Messages.Core_Case_is_closed, ch.elexis.core.ui.views.Messages.KonsDetailView_CaseClosedBody);
		} else {
			MessageDialog msd = new MessageDialog(getShell(), ch.elexis.core.ui.views.Messages.KonsDetailView_ChangeCaseCaption,
					Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize),
					MessageFormat.format(ch.elexis.core.ui.views.Messages.KonsDetailView_ConfirmChangeConsToCase,
							new Object[] { fallLabel, changeToCoverage.getLabel() }),
					MessageDialog.QUESTION, new String[] { ch.elexis.core.ui.views.Messages.Core_Yes, ch.elexis.core.ui.views.Messages.Corr_No }, 0);

			if (msd.open() == Window.OK) {
				if (tkv.actEncounter != null) {
					Result<IEncounter> transferResult = EncounterServiceHolder.get()
							.transferToCoverage(tkv.actEncounter, changeToCoverage, false);
					if (!transferResult.isOK()) {
						SWTHelper.alert("Error", transferResult.toString());
					}
				}
			}
		}
	}

	void refreshCases(IEncounter actEncounter) {
		if (isDisposed() || coverageLink == null || coverageLink.isDisposed()) {
			return;
		}

		IPatient pat = ContextServiceHolder.get().getRootContext().getTyped(IPatient.class).orElse(null);
		currentPatient = pat;

		if (actEncounter != null) {
			currentCoverage = actEncounter.getCoverage();
			updateLinkText(currentCoverage);
			updateLinkColor(currentCoverage);
		} else {
			currentCoverage = null;
			coverageLink.setText(Messages.CasesComposite_NoCase_Selected);
			coverageLink.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		}
	}

	void setEncounter(ICoverage coverage) {
		if (isDisposed() || coverageLink == null || coverageLink.isDisposed()) {
			return;
		}

		currentCoverage = coverage;
		updateLinkText(coverage);
		updateLinkColor(coverage);
		coverageLink.setEnabled(coverage != null && coverage.isOpen());
	}

	private void updateLinkText(ICoverage coverage) {
		if (isDisposed() || coverageLink == null || coverageLink.isDisposed()) {
			return;
		}

		if (coverage != null) {
			String label = coverage.getLabel();
			if (!coverage.isOpen()) {
				label += " " + Messages.CasesComposite_Closed_Suffix;
			}
			coverageLink.setText(label);
		} else {
			coverageLink.setText(Messages.CasesComposite_NoCase_Selected);
		}
		layout(true);
	}

	private void updateLinkColor(ICoverage coverage) {
		if (isDisposed() || coverageLink == null || coverageLink.isDisposed()) {
			return;
		}

		if (coverage != null) {
			// Use similar color logic as CoverageColorLabelProvider
			Color color;
			if (!coverage.isOpen()) {
				color = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
			} else {
				color = getDisplay().getSystemColor(SWT.COLOR_BLUE);
			}
			coverageLink.setForeground(color);
		} else {
			coverageLink.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		}
	}
}

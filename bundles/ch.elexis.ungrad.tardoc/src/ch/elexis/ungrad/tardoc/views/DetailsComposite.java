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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.ac.EvACEs;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.Mandant;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

/**
 * Composite for displaying and editing date and mandator of the current encounter.
 */
public class DetailsComposite extends Composite {
	private TardocKonsView tkv;
	private Hyperlink hlMandant, hlDate;
	private FormToolkit tk = UiDesk.getToolkit();

	public DetailsComposite(Composite parent, TardocKonsView view) {
		super(parent, SWT.NONE);
		this.tkv = view;
		setLayout(new RowLayout(SWT.HORIZONTAL));
		GridData gdDesc = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gdDesc.widthHint = 250;
		setLayoutData(gdDesc);
		// emFont = UiDesk.getFont("Helvetica", 11, SWT.BOLD); //$NON-NLS-1$
		// defaultBackground = p.getBackground();
		hlDate = tk.createHyperlink(this, "---", SWT.NONE);
		// hlDate.setFont(emFont);
		hlDate.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				GlobalActions.redateAction.reflectRight();
				if (GlobalActions.redateAction.isEnabled()) {
					GlobalActions.redateAction.doRun();
					DetailsComposite.this.setEncounter(DetailsComposite.this.tkv.actEncounter);
				}
			}
		});

		hlMandant = tk.createHyperlink(this, "--", SWT.NONE); //$NON-NLS-1$
		hlMandant.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				// CommonViewer of KontaktSelektor will set Mandant selection of
				// ElexisEventDispatcher
				// we want do reset to current mandant afterwards
				Mandant currentMandant = ElexisEventDispatcher.getSelectedMandator();
				KontaktSelektor ksl = new KontaktSelektor(DetailsComposite.this.tkv.getSite().getShell(), Mandant.class,
						Messages.Core_Select_Mandator, // $NON-NLS-1$
						Messages.KonsDetailView_SelectMandatorBody,
						new String[] { Mandant.FLD_SHORT_LABEL, Mandant.FLD_NAME1, Mandant.FLD_NAME2 }); // $NON-NLS-1$
				ksl.disableContextSelection();
				if (ksl.open() == Dialog.OK) {
					IMandator mandator = CoreModelServiceHolder.get()
							.load(((Mandant) ksl.getSelection()).getId(), IMandator.class).orElse(null);
					if (mandator != null) {
						Result<IEncounter> result = EncounterServiceHolder.get()
								.transferToMandator(DetailsComposite.this.tkv.actEncounter, mandator, false);
						if (!result.isOK()) {
							MessageDialog.openError(DetailsComposite.this.tkv.getSite().getShell(), Messages.Core_Error,
									result.getCombinedMessages());
						}
					}
				}
				ElexisEventDispatcher.fireSelectionEvent(currentMandant);
			}

		});
		hlMandant.setBackground(parent.getBackground());

	}

	void setEncounter(IEncounter enc) {
		if (isDisposed() || hlDate == null || hlDate.isDisposed() || hlMandant == null || hlMandant.isDisposed()) {
			return;
		}
		
		if (enc == null) {
			hlDate.setText("---"); //$NON-NLS-1$
			hlMandant.setText("---"); //$NON-NLS-1$
		} else {
			IMandator mandator = enc.getMandator();
			String encounterDate = TimeUtil.formatSafe(enc.getDate());
			hlDate.setText(encounterDate + " (" //$NON-NLS-1$
					+ new TimeTool(enc.getDate()).getDurationToNowString() + ")"); //$NON-NLS-1$
			StringBuilder sb = new StringBuilder();
			if (mandator == null) {
				sb.append(Messages.KonsDetailView_NotYours); // $NON-NLS-1$
				hlMandant.setBackground(hlMandant.getParent().getBackground());
			} else {
				IContact biller = mandator.getBiller();
				if (biller.getId().equals(mandator.getId())) {
					sb.append("(").append(mandator.getLabel()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					sb.append("(").append(mandator.getLabel()).append("/").append( //$NON-NLS-1$ //$NON-NLS-2$
							biller.getLabel()).append(")"); //$NON-NLS-1$
				}
				hlMandant.setBackground(UiMandant.getColorForMandator(Mandant.load(mandator.getId())));
			}
			hlMandant.setText(sb.toString());
			boolean hlMandantEnabled = BillingServiceHolder.get().isEditable(enc).isOK()
					&& AccessControlServiceHolder.get().evaluate(EvACEs.KONS_REASSIGN);
			hlMandant.setEnabled(hlMandantEnabled);
			// diagnosesDisplay.setEncounter(encounter);
			// billedDisplay.setEncounter(encounter);
			// billedDisplay.setEnabled(true);
			// diagnosesDisplay.setEnabled(true);

		}
	}
}

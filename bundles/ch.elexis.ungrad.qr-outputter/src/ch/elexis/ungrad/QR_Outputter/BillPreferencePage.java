/*******************************************************************************
 * Copyright (c) 2018-2026 by G. Weirich
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

package ch.elexis.ungrad.QR_Outputter;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

public class BillPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Combo cbMandanten;
	private Text txCaseVar;
	private Button bMailIfCaseVar;
	private Text txSubject, txBody;
	private Mandant currentMandator;
	IConfigService cfg = ConfigServiceHolder.get();

	public BillPreferencePage() {
		setDescription("Rechnungen");
	}

	public void init(IWorkbench workbench) {
	}

	private void setMandant(Mandant m) {
		currentMandator = m;
	}

	@Override
	protected Control createContents(Composite parent) {
		Color blau = UiDesk.getColor(UiDesk.COL_BLUE);
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(3, false));
		cbMandanten = new Combo(ret, SWT.READ_ONLY);
		cbMandanten.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		HashMap<String, Mandant> hMandanten = new HashMap<>();
		cbMandanten.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = cbMandanten.getSelectionIndex();
				if (i == -1) {
					return;
				}
				setMandant((Mandant) hMandanten.get(cbMandanten.getItem(i)));

			}

		});
		setMandant(ElexisEventDispatcher.getSelectedMandator());
		Query<Mandant> qbe = new Query<Mandant>(Mandant.class);
		List<Mandant> list = qbe.execute();

		for (Mandant m : list) {
			cbMandanten.add(m.getLabel());
			hMandanten.put(m.getLabel(), m);
		}
		cbMandanten.setText(currentMandator.getLabel());
		// Send Mail if Case Variable is set to Mailaddress
		bMailIfCaseVar = new Button(ret, SWT.CHECK);
		bMailIfCaseVar.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		bMailIfCaseVar.setText("Rechnungen bei Fällen mit folgender Eigenschaft per Mail senden");
		txCaseVar = new Text(ret, SWT.SINGLE);
		txCaseVar.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		String caseVar = cfg.get(PreferenceConstants.BY_MAIL_IF_CASEVAR, "");
		txCaseVar.setText(caseVar);
		bMailIfCaseVar.setSelection(!StringTool.isNothing(caseVar));
		Label lbSubject = new Label(ret, SWT.NONE);
		lbSubject.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		lbSubject.setText("Nachrichtentitel");
		txSubject = new Text(ret, SWT.NONE);
		txSubject.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		txSubject.setText(cfg.get(PreferenceConstants.BY_MAIL_SUBJECT, ""));
		Label lbBody = new Label(ret, SWT.NONE);
		lbBody.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		lbBody.setText("Standard-Nachrichtentext");
		txBody = new Text(ret, SWT.MULTI);
		txBody.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		txBody.setText(cfg.get(PreferenceConstants.BY_MAIL_BODY, ""));
		return ret;
	}

	private boolean applyFields() {
		if (bMailIfCaseVar.getSelection()) {
			cfg.set(PreferenceConstants.BY_MAIL_IF_CASEVAR, txCaseVar.getText());

		} else {
			cfg.set(PreferenceConstants.BY_MAIL_IF_CASEVAR, "");
		}
		return true;
	}

	@Override
	public boolean performOk() {
		return applyFields() && super.performOk();
	}

}
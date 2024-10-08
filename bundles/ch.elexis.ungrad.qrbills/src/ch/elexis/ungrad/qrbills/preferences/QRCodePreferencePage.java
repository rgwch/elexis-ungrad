/*******************************************************************************
 * Copyright (c) 2018-2024 by G. Weirich
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
package ch.elexis.ungrad.qrbills.preferences;

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

public class QRCodePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Combo cbMandanten;
	private Text txCaseVar;
	private Button bMailIfCaseVar;
	private Hyperlink hlBank;
	private Text txBank;
	private Label lbIban;
	private Text txIban, txSubject, txBody;
	private FileFieldEditor ffe1, ffe2, ffe3, ffe4;
	private Mandant currentMandator;
	private static final String ERRMSG_BAD_IBAN = "Eine zulässige QR-IBAN muss genau 21 Zeichen lang sein und mit CH oder LI beginnen.";
	private static final String ERRMSG_BAD_BANK = "Die gewählte Bank ist nicht gültig.";
	IConfigService cfg = ConfigServiceHolder.get();


	public QRCodePreferencePage() {
		setDescription("QR-Rechnungen");
	}

	public void init(IWorkbench workbench) {
	}

	private void setMandant(Mandant m) {
		currentMandator = m;
		String bankID = (String) currentMandator.getExtInfoStoredObjectByKey(PreferenceConstants.QRBANK);
		if (bankID == null) {
			txBank.setText("");
		} else {
			Kontakt bank = Kontakt.load(bankID);
			if (bank.isValid()) {
				txBank.setText(bank.getLabel());
			} else {
				txBank.setText("");
			}
		}
		String iban = (String) currentMandator.getExtInfoStoredObjectByKey(PreferenceConstants.QRIBAN);
		if (iban == null) {
			txIban.setText("");
		} else {
			txIban.setText(iban);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Color blau = UiDesk.getColor(UiDesk.COL_BLUE);
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(3, false));
		cbMandanten = new Combo(ret, SWT.READ_ONLY);
		cbMandanten.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		hlBank = new Hyperlink(ret, SWT.NONE);
		hlBank.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		hlBank.setText("Bank");
		hlBank.setForeground(blau);
		txBank = new Text(ret, SWT.READ_ONLY);
		txBank.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		lbIban = new Label(ret, SWT.NONE);
		lbIban.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		lbIban.setText("QR-IBAN");
		txIban = new Text(ret, SWT.NONE);
		txIban.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
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
		hlBank.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				KontaktSelektor ksel = new KontaktSelektor(getShell(), Kontakt.class, "Bank auswählen",
						"Bitte wählen Sie die Bank aus", new String[] { "Bezeichnung1", "Bezeichnung2" });
				if (ksel.open() == Dialog.OK) {
					Kontakt bank = (Kontakt) ksel.getSelection();
					currentMandator.setExtInfoStoredObjectByKey(PreferenceConstants.QRBANK, bank.getId());
					txBank.setText(bank.getLabel());
				}
			}

		});
		txIban.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				applyFields();
			}
		});
		ffe1 = new FileFieldEditor(PreferenceConstants.TEMPLATE_BILL, "Vorlage für Rechnung", ret);
		ffe2 = new FileFieldEditor(PreferenceConstants.TEMPLATE_REMINDER1, "Vorlage für Mahnung 1", ret);
		ffe3 = new FileFieldEditor(PreferenceConstants.TEMPLATE_REMINDER2, "Vorlage für Mahnung 2", ret);
		ffe4 = new FileFieldEditor(PreferenceConstants.TEMPLATE_REMINDER3, "Vorlage für Mahnung 3", ret);
		ffe1.setStringValue(cfg.get(PreferenceConstants.TEMPLATE_BILL, ""));
		ffe2.setStringValue(cfg.get(PreferenceConstants.TEMPLATE_REMINDER1, ""));
		ffe3.setStringValue(cfg.get(PreferenceConstants.TEMPLATE_REMINDER2, ""));
		ffe4.setStringValue(cfg.get(PreferenceConstants.TEMPLATE_REMINDER3, ""));
		
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
		cfg.set(PreferenceConstants.TEMPLATE_BILL, ffe1.getStringValue());
		cfg.set(PreferenceConstants.TEMPLATE_REMINDER1, ffe2.getStringValue());
		cfg.set(PreferenceConstants.TEMPLATE_REMINDER2, ffe3.getStringValue());
		cfg.set(PreferenceConstants.TEMPLATE_REMINDER3, ffe4.getStringValue());
		cfg.set(PreferenceConstants.BY_MAIL_SUBJECT, txSubject.getText());
		cfg.set(PreferenceConstants.BY_MAIL_BODY, txBody.getText());

		String iban = txIban.getText().toUpperCase();
		if (iban.length() != 21 || (!iban.startsWith("CH") && !iban.startsWith("LI"))) {
			SWTHelper.showError("IBAN", ERRMSG_BAD_IBAN);
			return false;
		} else {
			currentMandator.setExtInfoStoredObjectByKey(PreferenceConstants.QRIBAN, iban);
			return true;
		}
	}

	@Override
	public boolean performOk() {
		return applyFields() && super.performOk();
	}

}
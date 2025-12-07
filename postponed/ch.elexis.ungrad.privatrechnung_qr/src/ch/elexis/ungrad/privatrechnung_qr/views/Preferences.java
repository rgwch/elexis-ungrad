/*******************************************************************************
 * Copyright (c) 2007-2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.privatrechnung_qr.views;

import java.util.List;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore.Scope;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.ComboFieldEditor;
import ch.elexis.core.ui.preferences.inputs.KontaktFieldEditor;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.ungrad.privatrechnung_qr.data.PreferenceConstants;
import ch.rgw.io.Settings;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	Settings localCfg, globalCfg;
	List<Mandant> lMandanten;
	ComboFieldEditor cfe;
	FileFieldEditor ffBill, ffRem1, ffRem2, ffRem3, ffSecond;
	StringFieldEditor   sfQRIban, sfKunde;
	IntegerFieldEditor ifh1, if2nd;
	KontaktFieldEditor kfBank;
	Mandant selected;
	ConfigServicePreferenceStore csp=new ConfigServicePreferenceStore(Scope.LOCAL);
	static final String doSelect = "bitte wählen";
	
	public Preferences(){
		super(GRID);
		localCfg = CoreHub.localCfg;
		globalCfg = CoreHub.globalCfg;
		setPreferenceStore(new SettingsPreferenceStore(localCfg));
	}
	
	@Override
	protected void createFieldEditors(){
		lMandanten = Hub.getMandantenList();
		String[] fields = new String[lMandanten.size()];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = lMandanten.get(i).getLabel();
		}
		cfe = new ComboFieldEditor(PreferenceConstants.cfgBase, "Mandant", fields,
			getFieldEditorParent());
		ffBill = new FileFieldEditor(PreferenceConstants.TEMPLATE_BILL, "Vorlage für Rechnung",
			getFieldEditorParent());
		ffRem1 = new FileFieldEditor(PreferenceConstants.TEMPLATE_REMINDER1, "Erste Mahnung",
			getFieldEditorParent());
		ffRem2 = new FileFieldEditor(PreferenceConstants.TEMPLATE_REMINDER2, "Zweite Mahnung",
			getFieldEditorParent());
		ffRem3 = new FileFieldEditor(PreferenceConstants.TEMPLATE_REMINDER3, "Dritte Mahnung",
			getFieldEditorParent());
		ffSecond = new FileFieldEditor(PreferenceConstants.TEMPLATE_PAGE2, "Folgeseiten",
			getFieldEditorParent());
		ifh1 = new IntegerFieldEditor(PreferenceConstants.AVAILABLE_SPACE_1,
			"Verfügbare Höhe erste Seite (cm)", getFieldEditorParent());
		if2nd = new IntegerFieldEditor(PreferenceConstants.AVAILABLE_SPACE_2,
			"Verfügbare Höhe Folgeseiten (cm)", getFieldEditorParent());
		kfBank = new KontaktFieldEditor(csp, PreferenceConstants.cfgBank, "Bank",
			getFieldEditorParent());
		sfQRIban =
			new StringFieldEditor(PreferenceConstants.QRIBAN, "QR-IBAN", getFieldEditorParent());
		sfKunde = new IntegerFieldEditor(PreferenceConstants.bankClient, "Bank-Kundennummer",
			getFieldEditorParent());
		addField(cfe);
		addField(ffBill);
		addField(ffRem1);
		addField(ffRem2);
		addField(ffRem2);
		addField(ifh1);
		addField(if2nd);
		addField(kfBank);
		addField(sfQRIban);
		addField(sfKunde);
		cfe.getCombo().addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				if (selected != null) {
					flush(selected);
				}
				String mandLabel = cfe.getCombo().getText();
				Mandant m = getMandant(mandLabel);
				if (m != null) {
					selected = m;
					load(selected);
				}
			}
			
		});
		selected = null; // lMandanten.get(0);
		// cfe.getCombo().setText(selected.getLabel());
	}
	
	public void flush(Mandant m){
		if (m != null) {
			String id = m.getId();
			localCfg.set(PreferenceConstants.TEMPLATE_BILL + "/" + id, ffBill.getStringValue());
			localCfg.set(PreferenceConstants.TEMPLATE_REMINDER1 + "/" + id, ffRem1.getStringValue());
			localCfg.set(PreferenceConstants.TEMPLATE_REMINDER2 + "/" + id, ffRem2.getStringValue());
			localCfg.set(PreferenceConstants.TEMPLATE_REMINDER3 + "/" + id, ffRem3.getStringValue());
			localCfg.set(PreferenceConstants.AVAILABLE_SPACE_1 + "/" + id, ifh1.getStringValue());
			localCfg.set(PreferenceConstants.AVAILABLE_SPACE_2 + "/" + id, if2nd.getStringValue());
			Kontakt kBank = kfBank.getValue();
			if (kBank != null) {
				globalCfg.set(PreferenceConstants.cfgBank + "/" + id, kfBank.getValue().getId());
			}
			globalCfg.set(PreferenceConstants.QRIBAN + "/" + id, sfQRIban.getStringValue());
			globalCfg.set(PreferenceConstants.bankClient + "/" + id, sfKunde.getStringValue());
		}
		
	}
	
	public void load(Mandant m){
		if (m != null) {
			String id = m.getId();
			
			ffBill.setStringValue(localCfg.get(PreferenceConstants.TEMPLATE_BILL + "/" + id, ""));
			ffRem1.setStringValue(localCfg.get(PreferenceConstants.TEMPLATE_REMINDER1 + "/" + id, ""));
			ffRem2.setStringValue(localCfg.get(PreferenceConstants.TEMPLATE_REMINDER2 + "/" + id, ""));
			ffRem3.setStringValue(localCfg.get(PreferenceConstants.TEMPLATE_REMINDER3 + "/" + id, ""));
			ifh1.setStringValue(localCfg.get(PreferenceConstants.AVAILABLE_SPACE_1 + "/" + id, ""));
			if2nd.setStringValue(localCfg.get(PreferenceConstants.AVAILABLE_SPACE_2 + "/" + id, ""));	
			kfBank.set(Kontakt.load(globalCfg.get(PreferenceConstants.cfgBank + "/" + id, "")));
			sfQRIban.setStringValue(globalCfg.get(PreferenceConstants.QRIBAN + "/" + id, ""));
			sfKunde.setStringValue(globalCfg.get(PreferenceConstants.bankClient + "/" + id, ""));
		}
	}
	
	@Override
	public void init(IWorkbench workbench){
		
	}
	
	public Mandant getMandant(String label){
		for (Mandant m : lMandanten) {
			if (m.getLabel().equals(label)) {
				return m;
			}
		}
		return null;
	}
	
	@Override
	public boolean performOk(){
		if (selected != null) {
			flush(selected);
		}
		localCfg.flush();
		globalCfg.flush();
		return true;
	}
	
}

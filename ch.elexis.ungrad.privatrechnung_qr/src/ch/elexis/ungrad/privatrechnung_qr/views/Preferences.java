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
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.ComboFieldEditor;
import ch.elexis.core.ui.preferences.inputs.KontaktFieldEditor;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.ungrad.privatrechnung_qr.data.PreferenceConstants;
import ch.rgw.io.Settings;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	Settings cfg;
	List<Mandant> lMandanten;
	ComboFieldEditor cfe;
	StringFieldEditor sfQR, sf1st, sf2nd, sfQRIban, sfKunde;
	IntegerFieldEditor ifh1, if2nd;
	KontaktFieldEditor kfBank;
	Mandant selected;
	static final String doSelect = "bitte wählen";
	
	public Preferences(){
		super(GRID);
		cfg = CoreHub.localCfg;
		setPreferenceStore(new SettingsPreferenceStore(cfg));
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
		sfQR = new StringFieldEditor(PreferenceConstants.cfgTemplateQR, "Vorlage mit QR",
			getFieldEditorParent());
		sf1st = new StringFieldEditor(PreferenceConstants.cfgTemplateBill, "Erste Folgeseite",
			getFieldEditorParent());
		ifh1 = new IntegerFieldEditor(PreferenceConstants.cfgTemplateBillHeight,
			"Verfügbare Höhe erste Seite (cm)", getFieldEditorParent());
		sf2nd = new StringFieldEditor(PreferenceConstants.cfgTemplateBill2,
			"Vorlage weitere Seiten", getFieldEditorParent());
		if2nd = new IntegerFieldEditor(PreferenceConstants.cfgTemplateBill2Height,
			"Verfügbare Höhe Folgeseiten (cm)", getFieldEditorParent());
		kfBank = new KontaktFieldEditor(CoreHub.localCfg, PreferenceConstants.cfgBank, "Bank",
			getFieldEditorParent());
		sfQRIban =
			new StringFieldEditor(PreferenceConstants.QRIBAN, "QR-IBAN", getFieldEditorParent());
		sfKunde = new StringFieldEditor(PreferenceConstants.bankClient, "Bank-Kundennummer",
			getFieldEditorParent());
		addField(cfe);
		addField(sfQR);
		addField(sf1st);
		addField(ifh1);
		addField(sf2nd);
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
			cfg.set(PreferenceConstants.cfgTemplateQR + "/" + id, sfQR.getStringValue());
			cfg.set(PreferenceConstants.cfgTemplateBill + "/" + id, sf1st.getStringValue());
			cfg.set(PreferenceConstants.cfgTemplateBillHeight + "/" + id, ifh1.getStringValue());
			cfg.set(PreferenceConstants.cfgTemplateBill2 + "/" + id, sf2nd.getStringValue());
			cfg.set(PreferenceConstants.cfgTemplateBill2Height + "/" + id, if2nd.getStringValue());
			Kontakt kBank = kfBank.getValue();
			if (kBank != null) {
				cfg.set(PreferenceConstants.cfgBank + "/" + id, kfBank.getValue().getId());
			}
			cfg.set(PreferenceConstants.QRIBAN + "/" + id, sfQRIban.getStringValue());
			cfg.set(PreferenceConstants.bankClient + "/" + id, sfKunde.getStringValue());
		}
		
	}
	
	public void load(Mandant m){
		if (m != null) {
			String id = m.getId();
			
			sfQR.setStringValue(cfg.get(PreferenceConstants.cfgTemplateQR + "/" + id, ""));
			sf1st.setStringValue(cfg.get(PreferenceConstants.cfgTemplateBill + "/" + id, ""));
			ifh1.setStringValue(cfg.get(PreferenceConstants.cfgTemplateBillHeight + "/" + id, ""));
			sf2nd.setStringValue(cfg.get(PreferenceConstants.cfgTemplateBill2 + "/" + id, ""));
			if2nd
				.setStringValue(cfg.get(PreferenceConstants.cfgTemplateBill2Height + "/" + id, ""));
			kfBank.set(Kontakt.load(cfg.get(PreferenceConstants.cfgBank + "/" + id, "")));
			sfQRIban.setStringValue(cfg.get(PreferenceConstants.QRIBAN + "/" + id, ""));
			sfKunde.setStringValue(cfg.get(PreferenceConstants.bankClient + "/" + id, ""));
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
		CoreHub.localCfg.flush();
		return true;
	}
	
}

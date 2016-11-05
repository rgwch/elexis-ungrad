/*******************************************************************************
 * Copyright (c) 2007-2016, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  G. Weirich 5.11.2016: Migrated from 1.4 to 3.ungrad
 *******************************************************************************/
package ch.elexis.buchhaltung.kassenbuch;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class BuchungsDialog extends TitleAreaDialog {
	
	boolean bType;
	LabeledInputField liBeleg, liDate, liBetrag;
	Text text;
	KassenbuchEintrag last, act;
	Combo cbCats;
	
	BuchungsDialog(Shell shell, boolean mode){
		super(shell);
		bType = mode;
		act = null;
	}
	
	BuchungsDialog(Shell shell, KassenbuchEintrag kbe){
		super(shell);
		act = kbe;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		Composite top = new Composite(ret, SWT.BORDER);
		top.setLayout(new FillLayout());
		top.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		liBeleg = new LabeledInputField(top, "Beleg");
		liDate = new LabeledInputField(top, "Datum", LabeledInputField.Typ.DATE);
		liBetrag = new LabeledInputField(top, "Betrag", LabeledInputField.Typ.MONEY);
		Composite cCats = new Composite(ret, SWT.NONE);
		cCats.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cCats.setLayout(new GridLayout(2, false));
		new Label(cCats, SWT.NONE).setText("Kategorie");
		cbCats = new Combo(cCats, SWT.SINGLE);
		cbCats.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbCats.setItems(KassenbuchEintrag.getCategories());
		new Label(ret, SWT.NONE).setText("Buchungstext");
		text = new Text(ret, SWT.BORDER);
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		if (act == null) {
			last = KassenbuchEintrag.recalc();
			liBeleg.setText(KassenbuchEintrag.nextNr(last));
		} else {
			liBeleg.setText(act.getBelegNr());
			liDate.setText(act.getDate());
			liBetrag.setText(act.getAmount().getAmountAsString());
			cbCats.setText(act.getKategorie());
			text.setText(act.getText());
		}
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		if (act == null) {
			if (bType) {
				setTitle("Einnahme verbuchen");
			} else {
				setTitle("Ausgabe verbuchen");
			}
		} else {
			setTitle("Buchung ändern");
		}
		setMessage("Bitte geben Sie den Betrag und einen Buchungstext ein");
		getShell().setText("Buchung für Kassenbuch");
		liBetrag.getControl().setFocus();
	}
	
	@Override
	protected void okPressed(){
		Money money = new Money();
		try {
			money.addAmount(liBetrag.getText());
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		TimeTool tt = new TimeTool(liDate.getText());
		String bt = text.getText();
		
		if (act == null) {
			if (!bType) {
				money = money.negate();
			}
			act =
				new KassenbuchEintrag(liBeleg.getText(), tt.toString(TimeTool.DATE_GER), money, bt,
					last);
		} else {
			act.set(new String[] {
				"BelegNr", "Datum", "Betrag", "Text"
			}, liBeleg.getText(), tt.toString(TimeTool.DATE_GER), money.getCentsAsString(), text
				.getText());
			KassenbuchEintrag.recalc();
		}
		act.setKategorie(cbCats.getText());
		super.okPressed();
	}
	
}

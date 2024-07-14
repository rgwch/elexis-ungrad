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

package ch.elexis.ungrad.lucinda.omnivore;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.lucinda.Preferences;

public class ImportSettings extends TitleAreaDialog {
	Text tExclude;
	Button cbMove;
	
	public ImportSettings(Shell parentShell) {
		super(parentShell);
	}

	
	@Override
	public void create() {
		super.create();
		setTitle("Omnivore nach Lucinda");
		setMessage("Dokumente aus Omnivore indizieren und ggf. verschieben");
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ctl= (Composite)super.createDialogArea(parent);
		ctl.setLayout(new GridLayout(2,false));
		ctl.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		Label lbExclude=new Label(ctl, SWT.NONE);
		lbExclude.setText("Auszuschliessende Omnivore-Kategorien (Komma-getrennte Liste)");
		lbExclude.setLayoutData(SWTHelper.getFillGridData(2,true,1,false));
		tExclude=new Text(ctl,SWT.MULTI);
		tExclude.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		tExclude.setText(Preferences.get(Preferences.OMNIVORE_EXCLUDE, ""));
		Label lbMove=new Label(ctl,SWT.NONE);
		lbMove.setText("Dokumente nach Lucinda verschieben und aus Omnivore löschen");
		cbMove=new Button(ctl, SWT.CHECK);
		cbMove.setSelection(Preferences.is(Preferences.OMNIVORE_MOVE));
		return ctl;
	}

	private void save(){
		Preferences.set(Preferences.OMNIVORE_MOVE, cbMove.getSelection());
		Preferences.set(Preferences.OMNIVORE_EXCLUDE, tExclude.getText());
	}
	
	@Override
	protected void okPressed() {
		save();
		super.okPressed();
	}
	
}

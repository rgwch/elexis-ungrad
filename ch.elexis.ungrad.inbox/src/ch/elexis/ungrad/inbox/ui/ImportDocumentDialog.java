/*******************************************************************************
 * Copyright (c) 2023, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.inbox.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Person;
import ch.elexis.ungrad.inbox.model.DocumentDescriptor;

public class ImportDocumentDialog extends TitleAreaDialog {
	DocumentDescriptor dd;
	Text text;
	// Label lPat;
	String result = "";
	private View view;

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData());
		ret.setLayout(new GridLayout(2, false));
		// lPat = new Label(ret, SWT.NONE);
		Button bSelect = new Button(ret, SWT.PUSH);
		bSelect.setText("Patient/in zuweisen...");
		bSelect.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				KontaktSelektor ksl = new KontaktSelektor(getShell(), Person.class, "Bitte Patient/in wählen",
						"Patient/in für Zuordnung", null);
				if (ksl.open() == Dialog.OK) {
					Person pat = (Person) ksl.getSelection();
					setErrorMessage(null);
					setMessage(pat.getLabel());
					dd.concerns = pat;
				}
			}

		});
		if (dd.concerns == null) {
			setErrorMessage("Bitte weisen Sie eine Patientin oder einen Patienten zu");
		} else {
			setMessage(dd.concerns.getLabel());
		}
		Composite cText=new Composite(ret,SWT.NONE);
		cText.setLayoutData(SWTHelper.getFillGridData(2,true,1,false));
		cText.setLayout(new GridLayout(2, false));
		text = new Text(cText, SWT.BORDER);
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bView=new Button(cText,SWT.PUSH);
		bView.setImage(Images.IMG_EYE_WO_SHADOW.getImage());
		bView.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				view.launchViewer(dd.file);
			}
			
		});
		text.setText(dd.filename);
		return ret;
	}

	@Override
	public void create() {
		super.create();
		super.getShell().setText("Dokument importieren");
		setTitle("Dieses Dokument gehört zu:");
	}

	public ImportDocumentDialog(View view, DocumentDescriptor dd) {
		super(view.getSite().getShell());
		this.view=view;
		this.dd = dd;
	}

	@Override
	protected void okPressed() {
		result = text.getText();
		super.okPressed();
	}

	public String getValue() {
		return result;
	}
}

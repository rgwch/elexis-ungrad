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
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Person;
import ch.elexis.ungrad.inbox.model.DocumentDescriptor;

public class ImportDocumentDialog extends TitleAreaDialog {
	DocumentDescriptor dd;
	Text text;
	Label lPat;
	String result="";
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData());
		ret.setLayout(new GridLayout(2, false));
		lPat = new Label(ret, SWT.NONE);
		Button bSelect = new Button(ret, SWT.PUSH);
		bSelect.setText("Ändern...");
		bSelect.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				KontaktSelektor ksl=new KontaktSelektor(getShell(), Person.class, "Bitte Patient wählen", "Patient für Zuordnung", null);
				if(ksl.open()==Dialog.OK) {
					Person pat=(Person) ksl.getSelection();
					lPat.setText("Zu Patient: "+pat.getLabel());
					dd.concerns=pat;
				}
			}
			
		});
		lPat.setText("Zu Patient: "+dd.concerns.getLabel());
		lPat.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		text = new Text(ret, SWT.BORDER);
		text.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		text.setText(dd.filename);
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Dokument zuweisen");
		setMessage("Bitte prüfen und korrigieren Sie die Zuordnung zu Patient und den Dateinamen");
	}

	public ImportDocumentDialog(Shell parentShell, DocumentDescriptor dd) {
		super(parentShell);
		this.dd = dd;
	}

	@Override
	protected void okPressed() {
		result=text.getText();
		super.okPressed();
	}

	public String getValue() {
		return result;
	}
}

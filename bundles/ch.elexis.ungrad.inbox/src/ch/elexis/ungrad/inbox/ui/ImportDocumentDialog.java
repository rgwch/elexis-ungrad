/*******************************************************************************
* Copyright (c) 2023-2026, G. Weirich and Elexis
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

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.ungrad.AIUtil;
import ch.elexis.ungrad.inbox.model.DocumentDescriptor;
import ch.rgw.io.FileTool;

/**
 * Dialog opened to allow the user to accept or modify a proposal for an
 * association of a file to a patient
 * 
 * @author gerry
 *
 */
public class ImportDocumentDialog extends TitleAreaDialog {
	DocumentDescriptor dd;
	Text text;
	Button cbUseKI;
	// Label lPat;
	String result = "";
	boolean bUseKI = AIUtil.useAI();
	private View view;

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData());
		ret.setLayout(new GridLayout(2, false));
		// lPat = new Label(ret, SWT.NONE);
		Button bSelect = new Button(ret, SWT.PUSH);
		bSelect.setText(Messages.ImportDocumentDialog_AssignPatient);
		bSelect.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				KontaktSelektor ksl = new KontaktSelektor(getShell(), Person.class,
						Messages.ImportDocumentDialog_SelectPatientTitle,
						Messages.ImportDocumentDialog_SelectPatientMessage, null);
				if (ksl.open() == Dialog.OK) {
					Person pat = (Person) ksl.getSelection();
					setErrorMessage(null);
					setMessage(pat.getLabel());
					dd.concern(pat);
				}
			}

		});
		Button bFileSystem = new Button(ret, SWT.PUSH);
		bFileSystem.setText(Messages.ImportDocumentDialog_Directory);
		bFileSystem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
				fd.setText(Messages.ImportDocumentDialog_SelectFileTitle);
				fd.setFileName(dd.filename);
				String fn = fd.open();
				if (fn != null) {
					if (FileTool.copyFile(dd.file, new File(fn), FileTool.FAIL_IF_EXISTS)) {
						dd.file.delete();
						view.reload();
						cancelPressed();
					}
				}
			}
		});
		if (dd.concerns() == false) {
			setErrorMessage(Messages.ImportDocumentDialog_ErrorNoPatient);
		} else {
			setMessage(Patient.load(dd.concerns_id).getLabel());
		}
		Composite cText = new Composite(ret, SWT.NONE);
		cText.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cText.setLayout(new GridLayout(2, false));
		text = new Text(cText, SWT.BORDER);
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bView = new Button(cText, SWT.PUSH);
		bView.setImage(Images.IMG_EYE_WO_SHADOW.getImage());
		bView.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				view.launchViewer(dd.file);
			}

		});
		text.setText(dd.filename);
		// boolean bUseKI=CoreHub.localCfg.get(PreferenceConstants.USE_AI, false);
		if (bUseKI) {
			Composite cUseKI = new Composite(ret, SWT.NONE);
			cUseKI.setLayoutData(SWTHelper.getFillGridData(1, false, 1, true));
			cUseKI.setLayout(new FillLayout());
			cbUseKI = new Button(cUseKI, SWT.CHECK);
			cbUseKI.setSelection(bUseKI);
			Label lUseKI = new Label(cUseKI, SWT.NONE);
			lUseKI.setText(Messages.ImportDocumentDialog_UseAI);
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		super.getShell().setText(Messages.ImportDocumentDialog_ShellTitle);
		setTitle(Messages.ImportDocumentDialog_Title);
	}

	public ImportDocumentDialog(View view, DocumentDescriptor dd) {
		super(view.getSite().getShell());
		this.view = view;
		this.dd = dd;
	}

	@Override
	protected void okPressed() {
		if (cbUseKI != null) {
			bUseKI = cbUseKI.getSelection();
		}
		result = text.getText();
		super.okPressed();
	}

	public String getValue() {
		return result;
	}
}

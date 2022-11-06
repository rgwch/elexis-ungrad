/*******************************************************************************
 * Copyright (c) 2022 by G. Weirich
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

package ch.elexis.ungrad;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;

public class MailDialog extends TitleAreaDialog {
	
	Text tTo;
	Text tSubject;
	Text tBody;
	List lAttachments;
	
	public String mailTo = "";
	public String sender = "";
	public String subject = "";
	public String body = "";
	public String[] attachments = new String[0];
	
	public MailDialog(Shell parentShell){
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData());
		ret.setLayout(new GridLayout(2, false));
		Label lbTo = new Label(ret, SWT.NONE);
		lbTo.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
		lbTo.setText("An");
		tTo = new Text(ret, SWT.BORDER);
		tTo.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tTo.setText(mailTo);
		tTo.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusLost(FocusEvent e){
				mailTo = tTo.getText();
				if (StringTool.isMailAddress(mailTo)) {
					setErrorMessage(null);
					setMessage("Mail von " + sender);
				} else {
					setErrorMessage("Es ist kein gültiger Addressat gesetzt");
				}
				super.focusLost(e);
			}
			
		});
		
		Label lbSubject = new Label(ret, SWT.NONE);
		lbSubject.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
		lbSubject.setText("Betreff");
		tSubject = new Text(ret, SWT.BORDER);
		tSubject.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tSubject.setText(subject);
		
		tBody = new Text(ret, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		GridData gd = new GridData(SWT.DEFAULT, 100);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		tBody.setLayoutData(gd);
		String esc = body.replace("<br />", "\n");
		tBody.setText(esc);
		
		Label lbAttachments = new Label(ret, SWT.NONE);
		lbAttachments.setLayoutData(SWTHelper.getFillGridData(2, false, 1, false));
		lbAttachments.setText("Anhänge");
		Button bAdd = new Button(ret, SWT.PUSH);
		bAdd.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
		bAdd.setText("Hinzu...");
		bAdd.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				FileDialog fld = new FileDialog(getShell());
				if (attachments.length > 0) {
					fld.setFilterPath(FileTool.getFilepath(attachments[0]));
				}
				fld.setFilterExtensions(new String[] {"*.pdf;*.doc;*.odt","*.*"});
				String result = fld.open();
				if (result != null) {
					lAttachments.add(result);
					ret.layout();
				}
			}
			
		});
		GridData lgd = new GridData(SWT.DEFAULT, 50);
		lgd.horizontalSpan = 1;
		lgd.horizontalAlignment = GridData.FILL;
		lAttachments = new List(ret, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		lAttachments.setLayoutData(lgd);
		lAttachments.setItems(attachments);
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Dokument als Mailanhang versenden");
		if (StringTool.isNothing(mailTo)) {
			setErrorMessage("Es ist kein gültiger Addressat gesetzt");
		} else {
			setMessage("Mail von: " + sender);
		}
	}
	
	@Override
	protected void okPressed(){
		mailTo = tTo.getText();
		subject = tSubject.getText();
		body = tBody.getText().replace("\n", "<br />");
		attachments = lAttachments.getItems();
		super.okPressed();
	}
	
}

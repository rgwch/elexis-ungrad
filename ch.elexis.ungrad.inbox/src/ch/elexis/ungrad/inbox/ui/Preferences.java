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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.inbox.model.PreferenceConstants;

public class Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	Text tEnter, tText;
	Label lResult;

	public Preferences() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription("Ungrad Inbox");
	}

	@Override
	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub

	}

	private String[][] methods = { { "Nein", "none" }, { "IMAP", "imap" }, { "Mbox", "mbox" } };

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.BASEDIR, "Verzeichnis", getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(PreferenceConstants.MAILMODE, "Emails einlesen", 3, methods,
				getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.MBOX, "MBox-Datei", getFieldEditorParent()));
		addField(new MultilineFieldEditor(PreferenceConstants.WHITELIST, "Absender", 4, SWT.V_SCROLL, false,
				getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.MAPPINGS, "Dateinamen-Analyse", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.ANALYZE_CONTENTS, "Auch Datei-Inhalt betrachten (Erfordert Lucinda)", getFieldEditorParent()));
		Composite p = getFieldEditorParent();
		Group cCheck = new Group(p, SWT.BORDER);
		cCheck.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		cCheck.setText("Regexp-Tester");
		cCheck.setLayout(new GridLayout(2, false));
		Label lEnter = new Label(cCheck, SWT.NONE);
		lEnter.setText("Regexp");
		tEnter = new Text(cCheck, SWT.BORDER);
		tEnter.setLayoutData(SWTHelper.getFillGridData());
		tEnter.addModifyListener(matchChecker);
		Label lText = new Label(cCheck, SWT.NONE);
		lText.setText("Zeichenfolge");
		tText = new Text(cCheck, SWT.BORDER);
		tText.setLayoutData(SWTHelper.getFillGridData());
		lResult = new Label(cCheck, SWT.NONE);
		lResult.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		tText.addModifyListener(matchChecker);

	}

	ModifyListener matchChecker = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent arg0) {
			Pattern pattern = Pattern.compile(tEnter.getText());
			Matcher matcher = pattern.matcher(tText.getText());
			if (matcher.matches()) {
				int num = matcher.groupCount();
				if (num == 0) {
					lResult.setText("Gefunden: " + matcher.group());
				} else {
					String res = "Treffer: ";
					for (int i = 1; i <= num; i++) {
						res = res + "(" + matcher.group(i) + ") ";
					}
					lResult.setText(res);
				}
			} else {
				lResult.setText("Keine Übereinstimmung");
			}
		}
	};

}

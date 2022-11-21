/**
 * Copyright (c) 2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 */

package ch.elexis.ungrad.text.templator.ui;

import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.text.templator.model.ODFDoc;

public class OdfTemplateFieldsDisplay extends Composite {
	private IAction printAction;
	private ch.elexis.core.ui.text.ITextPlugin.ICallback saveHandler;
	private Composite cFields;
	private ODFDoc doc;

	public OdfTemplateFieldsDisplay(Composite parent, ICallback handler) {
		super(parent, SWT.NONE);
		saveHandler = handler;
		if (parent.getLayout() instanceof GridLayout) {
			setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		}
		// setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		setLayout(new FillLayout());
		Composite body = new Composite(this, SWT.NONE);
		body.setLayout(new GridLayout());
		// body.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
		makeActions();
		ToolBarManager tbm = new ToolBarManager(SWT.HORIZONTAL);
		tbm.add(printAction);
		tbm.createControl(body);
		cFields = new Composite(body, SWT.NONE);
		// cFields.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		cFields.setLayout(new GridLayout(2, false));
		cFields.setLayoutData(SWTHelper.getFillGridData());
	}

	public void set(ODFDoc doc) {
		for (Control c : cFields.getChildren()) {
			// c.removeFocusListener(fs);
			c.dispose();
		}

		for (Entry<String, String> e : doc.getFields()) {
			Label lbl = new Label(cFields, SWT.NONE);
			lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Text text = new Text(cFields, SWT.BORDER);
			text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			// text.addFocusListener(fs);
			lbl.setText(e.getKey());
			text.setText(e.getValue());
			text.setData("field", e.getKey());
		}
		cFields.layout();
		this.doc = doc;
	}

	private void makeActions() {
		printAction = new Action("Ausgeben") {
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText("Gibt dieses Dokument mit dem konfigurierten Ausgabeprogramm aus");
			}

			@Override
			public void run() {
				try {
					doc.doOutput();
				} catch (Exception e) {
					SWTHelper.showError("Fehler bei Ausgabe", e.getMessage());

				}
			}
		};

	}
}

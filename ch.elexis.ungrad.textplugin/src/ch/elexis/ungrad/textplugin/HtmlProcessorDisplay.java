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

package ch.elexis.ungrad.textplugin;

import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.util.SWTHelper;

public class HtmlProcessorDisplay extends Composite {
	private HtmlDoc doc;
	ScrolledForm form;
	ICallback saveHandler; 
	
	public HtmlProcessorDisplay(Composite parent, HtmlDoc document, ICallback handler) {
		super(parent,SWT.NONE);
		doc=document;
		if (parent.getLayout() instanceof GridLayout) {
			setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		}
		setLayout(new FillLayout());
		form = UiDesk.getToolkit().createScrolledForm(this);
		saveHandler = handler;
		Composite body = form.getBody();
		body.setLayout(new GridLayout(2,false));
		for(Entry<String, String> e:doc.fields.entrySet()) {
			Label l=new Label(body, SWT.NONE);
			l.setText(e.getKey());
			l.setLayoutData(SWTHelper.getFillGridData());
			Text t=new Text(body,SWT.NONE);
			l.setText(e.getValue());
			l.setLayoutData(SWTHelper.getFillGridData());
		}
	}
}

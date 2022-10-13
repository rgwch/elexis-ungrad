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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	Composite cFields;
	Text tTemplate;
	boolean bSaveOnFocusLost=true;
	FocusSaver fs = new FocusSaver();

	
	public HtmlProcessorDisplay(Composite parent, ICallback handler) {
		super(parent, SWT.NONE);
		if (parent.getLayout() instanceof GridLayout) {
			setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		}
		setLayout(new FillLayout());
		form = UiDesk.getToolkit().createScrolledForm(this);
		saveHandler = handler;
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		tTemplate=new Text(body,SWT.READ_ONLY);
		tTemplate.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		cFields = new Composite(body, SWT.NONE);
		cFields.setLayoutData(SWTHelper.getFillGridData());
		cFields.setLayout(new GridLayout(2, false));
	}

	public void setDocument(HtmlDoc doc) {
		this.doc=doc;
		for (Control c : cFields.getChildren()) {
			c.removeFocusListener(fs);
			c.dispose();
		}
		for (Entry<String, String> e: doc.getFields().entrySet()) {
			Label lbl = new Label(cFields, SWT.NONE);
			lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Text text = new Text(cFields, SWT.BORDER);
			text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			text.addFocusListener(fs);
			lbl.setText(e.getKey());
			text.setText(e.getValue());
		}
		
		String templ = doc.fields.get("template");
		tTemplate.setText(templ == null ? "unbekannt" : templ);
		cFields.layout();
	
	}
	
	public void save(){
		//collect();
		saveHandler.save();
	}

	class FocusSaver extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e){
			if (bSaveOnFocusLost) {
				// proc.getProcessor().doOutput(proc);
				save();
			}
		}
		
	}

}

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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.WidgetFactory;

public class HtmlProcessorDisplay extends Composite {
	private HtmlDoc doc;
	ScrolledForm form;
	ICallback saveHandler;
	Composite cFields;
	Composite cAdditional;
	Text tTemplate;
	boolean bSaveOnFocusLost=true;
	FocusSaver fs = new FocusSaver();
	private IAction printAction, directOutputAction;
	private ExpandableComposite ecDefaults;
	private ListViewer lvDefaults;

	
	public HtmlProcessorDisplay(Composite parent, ICallback handler) {
		super(parent, SWT.NONE);
		if (parent.getLayout() instanceof GridLayout) {
			setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		}
		FormToolkit tk=UiDesk.getToolkit();
		setLayout(new FillLayout());
		form = tk.createScrolledForm(this);
		saveHandler = handler;
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		makeActions();
		ToolBarManager tbm = new ToolBarManager(SWT.HORIZONTAL);
		tbm.add(printAction);
		tbm.createControl(body);
		tTemplate=new Text(body,SWT.READ_ONLY);
		tTemplate.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		ecDefaults=new ExpandableComposite(body, SWT.NONE);
		ecDefaults.setText("Vorgabefelder");
		ecDefaults.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e){
				form.reflow(true);
			}
		});
		cFields = new Composite(ecDefaults, ExpandableComposite.TWISTIE);
		cFields.setLayoutData(SWTHelper.getFillGridData());
		cFields.setLayout(new GridLayout(2, false));
		ecDefaults.setClient(cFields);
		cAdditional=new Composite(body, SWT.NONE);
		cAdditional.setLayoutData(SWTHelper.getFillGridData());
		cAdditional.setLayout(new GridLayout(2,false));
	}

	public void setDocument(HtmlDoc doc) {
		this.doc=doc;
		for (Control c : cFields.getChildren()) {
			c.removeFocusListener(fs);
			c.dispose();
		}
		for(Control c: cAdditional.getChildren()) {
			c.dispose();
		}
		for (Entry<String, String> e: doc.getPrefilled().entrySet()) {
			Label lbl = new Label(cFields, SWT.NONE);
			lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Text text = new Text(cFields, SWT.BORDER);
			text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			text.addFocusListener(fs);
			lbl.setText(e.getKey());
			text.setText(e.getValue());
		}
		for(String key:doc.getPostfilled().keySet()) {
			Label lbl=new Label(cAdditional,SWT.NONE);
			lbl.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			lbl.setText(key);
			Text text=new Text(cAdditional,SWT.MULTI|SWT.BORDER);
			text.setLayoutData(SWTHelper.getFillGridData(2,true,2,true));
		}
		cFields.layout();
		
	}
	
	private void makeActions(){
		printAction = new Action("Ausgeben") {
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText("Gibt dieses Dokument mit dem konfigurierten Ausgabeprogramm aus");
			}
			
			@Override
			public void run(){
				save();
				try {
					doc.doOutput("");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
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

/*******************************************************************************
 * Copyright (c) 2022-2024 by G. Weirich
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
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.textplugin.HtmlDoc.Table;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public class HtmlProcessorDisplay extends Composite {
	private HtmlDoc doc;
	ScrolledForm form;
	ICallback saveHandler;
	Composite cFields;
	Composite cAdditional;
	Text tTemplate, tStructure, tInfo;
	boolean bSaveOnFocusLost = true;
	FocusSaver fs = new FocusSaver();
	private IAction printAction, directOutputAction, fieldDisplayAction, structureDisplayAction, infoDisplayAction;
	private ExpandableComposite ecDefaults;
	private ListViewer lvDefaults;
	private StackLayout stackLayout = new StackLayout();

	private Composite cFrame, cFieldDisplay, cStructureDisplay, cInfoDisplay;

	public HtmlProcessorDisplay(Composite parent, ICallback handler) {
		super(parent, SWT.NONE);
		if (parent.getLayout() instanceof GridLayout) {
			setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		}
		FormToolkit tk = UiDesk.getToolkit();
		setLayout(new GridLayout());
		makeActions();
		ToolBarManager tbm = new ToolBarManager(SWT.HORIZONTAL);
		tbm.add(printAction);
		tbm.add(fieldDisplayAction);
		tbm.add(structureDisplayAction);
		tbm.add(infoDisplayAction);
		tbm.createControl(this);
		cFrame = new Composite(this, SWT.NONE);
		cFrame.setLayoutData(SWTHelper.getFillGridData());
		cFrame.setLayout(stackLayout);
		cFieldDisplay = new Composite(cFrame, SWT.NONE);
		cStructureDisplay = new Composite(cFrame, SWT.V_SCROLL);
		cStructureDisplay.setLayout(new FillLayout());
		tStructure = new Text(cStructureDisplay, SWT.MULTI);
		cInfoDisplay = new Composite(cFrame, SWT.NONE);
		cInfoDisplay.setLayout(new FillLayout());
		tInfo = new Text(cInfoDisplay, SWT.MULTI);
		stackLayout.topControl = cFieldDisplay;
		cFieldDisplay.setLayout(new FillLayout());
		form = tk.createScrolledForm(cFieldDisplay);
		saveHandler = handler;
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		tTemplate = new Text(body, SWT.READ_ONLY);
		tTemplate.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		ecDefaults = new ExpandableComposite(body, SWT.NONE);
		ecDefaults.setText("Vorgabefelder");
		ecDefaults.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		cFields = new Composite(ecDefaults, ExpandableComposite.TWISTIE);
		cFields.setLayoutData(SWTHelper.getFillGridData());
		cFields.setLayout(new GridLayout(2, false));
		ecDefaults.setClient(cFields);
		cAdditional = new Composite(body, SWT.NONE);
		cAdditional.setLayoutData(SWTHelper.getFillGridData());
		cAdditional.setLayout(new GridLayout(2, false));

	}
	public void clear() {
		for (Control c : cFields.getChildren()) {
			c.removeFocusListener(fs);
			c.dispose();
		}
		for (Control c : cAdditional.getChildren()) {
			c.removeFocusListener(fs);
			c.dispose();
		}
	}

	public void setDocument(HtmlDoc doc) {
		this.doc = doc;
		if (doc.getFilename() != null) {
			asyncRunViewer(doc.getFilename());
		}
		clear();
		for (Entry<String, String> e : doc.getPrefilled().entrySet()) {
			Label lbl = new Label(cFields, SWT.NONE);
			lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Text text = new Text(cFields, SWT.BORDER);
			text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			text.addFocusListener(fs);
			lbl.setText(e.getKey());
			text.setText(e.getValue());
			text.setData("field", e.getKey());
		}
		for (Entry<String, Object> e : doc.getPostfilled().entrySet()) {
			Label lbl = new Label(cAdditional, SWT.NONE);
			lbl.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			lbl.setText(e.getKey().substring(1, e.getKey().length() - 1) + ":");
			Text text = new Text(cAdditional, SWT.MULTI | SWT.BORDER);
			text.setLayoutData(SWTHelper.getFillGridData(2, true, 2, true));
			text.addFocusListener(fs);
			text.setData("field", e.getKey());
			text.setText(createStringValue(e.getValue()));
		}
		cFields.layout();
		tStructure.setText(doc.getTemplate());
	}

	private String createStringValue(Object in) {
		if (in instanceof Table) {
			return "TABLE";
		} else {
			return (String) in;
		}
	}

	private String createDisplay(String in) {
		if (in.startsWith("<table>")) {
			String out = in.replaceAll("</tr>", "\\r").replaceAll("</td><td>", StringConstants.SPACE)
					.replaceAll("<br />", "\\r").replaceAll("<.+?>", "");
			return out;
		} else {
			return in;
		}
	}

	private void collect() {
		for (Control c : cFields.getChildren()) {
			Object field = c.getData("field");
			if (field != null) {
				doc.setPrefilled((String) field, ((Text) c).getText());
			}
		}
		for (Control c : cAdditional.getChildren()) {
			Object field = c.getData("field");
			if (field != null) {
				Object previous = doc.getPostfilled().get(field);
				if (previous instanceof String) {
					doc.setPostfilled((String) field, ((Text) c).getText());
				} // TODO; else if instanceof Table
			}
		}
	}

	private void makeActions() {
		printAction = new Action("Ausgeben") {
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText("Gibt dieses Dokument mit dem konfigurierten Ausgabeprogramm aus");
			}

			@Override
			public void run() {
				save();
				try {
					String outfile = doc.doOutput("");
					if (outfile != null) {
						asyncRunViewer(outfile);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		fieldDisplayAction = new Action("Feldanzeige") {
			{
				// setImageDescriptor(Images.IMG_CARDS.getImageDescriptor());
				setText("F");
			}

			@Override
			public void run() {
				stackLayout.topControl = cFieldDisplay;
				cFrame.layout();
			}
		};
		structureDisplayAction = new Action("Struktur") {
			{
				// setImageDescriptor(Images.IMG_CARDS.getImageDescriptor());
				setText("S");
			}

			@Override
			public void run() {
				stackLayout.topControl = cStructureDisplay;
				cFrame.layout();
			}
		};
		infoDisplayAction = new Action("Informationen") {
			{
				// setImageDescriptor(Images.IMG_CARDS.getImageDescriptor());
				setText("I");
			}

			@Override
			public void run() {
				stackLayout.topControl = cInfoDisplay;
				cFrame.layout();

			}
		};
	}

	public void openUnknown(String filepath) {
		asyncRunViewer(filepath);
	}
	
	private void asyncRunViewer(String filepath) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					String ext = FileTool.getExtension(filepath); // $NON-NLS-1$

					Program proggie = Program.findProgram(ext);
					if (proggie != null) {
						proggie.execute(filepath);
					} else {
						if (Program.launch(filepath) == false) {
							Runtime.getRuntime().exec(filepath);
						}
					}

				} catch (Exception ex) {
					ExHandler.handle(ex);
					SWTHelper.showError("Could not create or show file", ex.getMessage());
				}
			}

		});

	}

	public void save() {
		collect();
		saveHandler.save();
	}

	class FocusSaver extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			if (bSaveOnFocusLost) {
				// proc.getProcessor().doOutput(proc);
				save();
			}
		}

	}

}

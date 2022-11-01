/*******************************************************************************
 * Copyright (c) 2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.forms.ui;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.ungrad.forms.model.PreferenceConstants;
import ch.rgw.io.FileTool;

/**
 * @author gerry
 *
 */
public class SelectTemplateDialog extends TitleAreaDialog {

	TableViewer tv;
	File result;
	String templateDir;

	public SelectTemplateDialog(Shell parentShell) {
		super(parentShell);
		templateDir = CoreHub.localCfg.get(PreferenceConstants.TEMPLATES, "");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		tv = new TableViewer(ret, SWT.V_SCROLL);
		tv.setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object dir) {
				String[] templates = ((File) dir).list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						String ext = FileTool.getExtension(name).toLowerCase();
						if (ext.equals("pug") || ext.equals("html") || ext.equals("pdf")) {
							return true;
						} else {
							return false;
						}

					}
				});
				if (templates == null) {
					return new String[0];
				} else {
					return templates;
				}
			}
		});
		tv.setLabelProvider(new DefaultLabelProvider());
		tv.setComparator(new ViewerComparator());
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		tv.setInput(new File(templateDir));
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Formular auswählen");
	}

	@Override
	protected void okPressed() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if ((sel != null) && (!sel.isEmpty())) {
			result = new File(templateDir, (String) sel.getFirstElement());
		}
		super.okPressed();
	}

}

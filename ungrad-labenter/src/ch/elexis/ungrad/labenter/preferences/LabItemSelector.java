/*******************************************************************************
 * Copyright (c) 2018 by G. Weirich
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

package ch.elexis.ungrad.labenter.preferences;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultContentProvider;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.data.LabItem;

public class LabItemSelector extends TitleAreaDialog {

	CommonViewer cv = new CommonViewer();
	LabItem result;

	public LabItemSelector(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		setMessage("Bitte Labor Item auswählen");
		setTitle("Laboritems");
		composite.setLayout(SWTHelper.createGridLayout(true, 1));
		ViewerConfigurer vc = new ViewerConfigurer(new DefaultContentProvider(cv, LabItem.class),
				new DefaultLabelProvider(), new DefaultControlFieldProvider(cv, new String[] { LabItem.SHORTNAME }),
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LIST, SWT.NONE, cv));
		cv.create(vc, parent, SWT.NONE, this);
		vc.getContentProvider().startListening();
		return composite;
	}

	@Override
	protected void okPressed() {
		result = (LabItem) cv.getSelection()[0];
		super.okPressed();
	}

}

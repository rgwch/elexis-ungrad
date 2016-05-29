/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
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

package ch.rgw.elexis.docmgr_lucinda.view;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import ch.rgw.elexis.docmgr_lucinda.controller.Controller;
import ch.rgw.elexis.docmgr_lucinda.model.Document;

public class GlobalViewPane extends Composite {
	private SashForm sashForm;
	private Master master;
	private Detail detail;
	private ProgressBar progressBar;
	Controller controller;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public GlobalViewPane(Composite parent, Controller ctl) {
		super(parent, SWT.NONE);
		setLayout(new FormLayout());
		controller = ctl;
		sashForm = new SashForm(this, SWT.VERTICAL);
		master = new Master(sashForm, this);
		detail = new Detail(sashForm);
		sashForm.setWeights(new int[] { 4, 1 });
		FormData fdSash = new FormData();
		fdSash.top = new FormAttachment(0, 0);
		fdSash.left = new FormAttachment(0, 0);
		fdSash.right = new FormAttachment(100, 0);
		sashForm.setLayoutData(fdSash);
		progressBar = new ProgressBar(this, SWT.SMOOTH);
		FormData fdProgressBar = new FormData();
		fdProgressBar.bottom = new FormAttachment(100, 0);
		fdProgressBar.left = new FormAttachment(0, 0);
		fdProgressBar.right = new FormAttachment(100, 0);
		progressBar.setLayoutData(fdProgressBar);
		fdSash.bottom = new FormAttachment(progressBar, 0);
		progressBar.setMinimum(0);
		progressBar.setVisible(false);
	}

	public void initProgress(int maximum) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				progressBar.setMaximum(maximum);
				progressBar.setVisible(true);
			}

		});
	}

	public void showProgress(int position) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				progressBar.setSelection(position);
				progressBar.setToolTipText(position + " von " + progressBar.getMaximum()); //$NON-NLS-1$
			}

		});
	}

	public void finishProgress() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				progressBar.setVisible(false);
			}
		});
	}

	public void setSelection(Object element) {
		if (element == null) {
			detail.setInput(new HashMap<String, Object>());
		} else {
			detail.setInput(element);
		}
	}

	public void setConnected(boolean bConnected) {
		master.setConnected(bConnected);
	}

	public Text getSearchField() {
		return master.getSearchField();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void loadDocument(Object element) {
		controller.loadDocument(new Document(element));
	}

}

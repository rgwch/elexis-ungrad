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

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.icons.Images;
import ch.rgw.elexis.docmgr_lucinda.Activator;
import ch.rgw.elexis.docmgr_lucinda.controller.DocumentSorter;

public class Master extends Composite {

	private Text text;
	private Table table;
	private TableViewer tableViewer;
	private Label lblConnection;
	private Button btnGo;
	public static int COLUMN_TYPE = 0;
	public static int COLUMN_NAME = 1;
	public static int COLUMN_DATE = 2;
	public static int COLUMN_DOC = 3;
	String[] columnTitles = { Messages.Master_col_caption_type, Messages.Master_col_caption_patient, Messages.Master_col_caption_date, Messages.Master_col_caption_doc };
	int[] columnWidths = { 50, 100, 100, 150 };

	Master(final Composite parent, final GlobalViewPane gvp) {
		super(parent, SWT.NONE);
		setLayout(new FormLayout());
		Composite searchBox = new Composite(this, SWT.NONE);
		FormData fd_searchBox = new FormData();
		// fd_searchBox.bottom = new FormAttachment(0);
		fd_searchBox.right = new FormAttachment(100, 0);
		fd_searchBox.top = new FormAttachment(0);
		fd_searchBox.left = new FormAttachment(0);
		searchBox.setLayoutData(fd_searchBox);
		searchBox.setLayout(new FormLayout());

		text = new Text(searchBox, SWT.BORDER);
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				gvp.controller.runQuery(text.getText());
			}
		});
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				gvp.controller.clear();
			}
		});
		FormData fd_text = new FormData();
		fd_text.right = new FormAttachment(85);
		fd_text.top = new FormAttachment(0, 5);
		fd_text.left = new FormAttachment(0, 34);
		text.setLayoutData(fd_text);

		btnGo = new Button(searchBox, SWT.PUSH);
		FormData fd_btnGo = new FormData();
		// fd_btnGo.bottom = new FormAttachment(0, 32);
		fd_btnGo.right = new FormAttachment(100, -5);
		fd_btnGo.top = new FormAttachment(text, 0, SWT.CENTER);
		fd_btnGo.left = new FormAttachment(text, 5);
		btnGo.setLayoutData(fd_btnGo);
		btnGo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				gvp.controller.runQuery(text.getText());
			}
		});
		btnGo.setText(Messages.Master_searchButton_caption);

		lblConnection = new Label(searchBox, SWT.NONE);
		FormData fd_lblConnection = new FormData();
		fd_lblConnection.height = 16;
		fd_lblConnection.width = 16;
		fd_lblConnection.top = new FormAttachment(text, 0, SWT.CENTER);
		lblConnection.setLayoutData(fd_lblConnection);
		lblConnection.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {

			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				gvp.controller.reconnect();
			}

		});
		Label lblClear = new Label(searchBox, SWT.NONE);
		FormData fd_lblClear = new FormData();
		fd_lblClear.height = 16;
		fd_lblClear.width = 16;
		fd_lblClear.top = new FormAttachment(text, 0, SWT.CENTER);
		fd_lblClear.left = new FormAttachment(lblConnection, 0);
		lblClear.setLayoutData(fd_lblClear);
		lblClear.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {

			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				text.setText(""); //$NON-NLS-1$
			}

		});

		lblClear.setImage(Images.IMG_CLEAR.getImage());
		lblClear.setToolTipText(Messages.Master_clearButton_tooltip);
		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		table = tableViewer.getTable();
		FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100, 0);
		fd_table.right = new FormAttachment(100, 0);
		fd_table.top = new FormAttachment(searchBox, 0);
		fd_table.left = new FormAttachment(0, 0);
		table.setLayoutData(fd_table);
		tableViewer.setContentProvider(gvp.controller.getContentProvider(tableViewer));

		createColumns();

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tableViewer.setLabelProvider(gvp.controller.getLabelProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					gvp.setSelection(null);
				} else {
					gvp.setSelection(sel.getFirstElement());
				}
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					gvp.loadDocument(sel.getFirstElement());
				}

			}
		});

		setConnected(false);

	}

	private void createColumns() {
		for (int i = 0; i < columnTitles.length; i++) {
			TableViewerColumn tvc = new TableViewerColumn(tableViewer, SWT.NULL);
			TableColumn tc = tvc.getColumn();
			tc.setText(columnTitles[i]);
			tc.setWidth(columnWidths[i]);
			addHeaderListener(tc, i);
		}
	}

	
	private void addHeaderListener(final TableColumn tc, int index) {
		tc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tc.getData("direction") == null) { //$NON-NLS-1$
					tc.setData("direction", false); //$NON-NLS-1$
				}
				boolean bDirec = !(Boolean) tc.getData("direction"); //$NON-NLS-1$
				tableViewer.setSorter(new DocumentSorter(index, bDirec));
				tc.setData("direction", bDirec); //$NON-NLS-1$
			}

		});
	}

	public void setConnected(boolean bConnected) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (bConnected) {
					StringBuilder sb = new StringBuilder(Messages.Master_connected_tooltip);
					if (Activator.getDefault().isBusAPI()) {
						sb.append("EventBus "); //$NON-NLS-1$
					}
					if (Activator.getDefault().isRestAPI()) {
						sb.append("REST"); //$NON-NLS-1$
					}
					sb.append(Messages.Master_connected_tooltip2);
					lblConnection.setImage(Images.IMG_BULLET_GREEN.getImage());
					lblConnection.setToolTipText(sb.toString());
				} else {
					lblConnection.setImage(Images.IMG_BULLET_RED.getImage());
					lblConnection.setToolTipText(Messages.Master_disconnected_tooltip);
				}
				text.setEnabled(bConnected);
				btnGo.setEnabled(bConnected);

			}

		});
	}

	public Text getSearchField() {
		return text;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}

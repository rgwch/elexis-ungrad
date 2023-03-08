/*******************************************************************************
 * Copyright (c) 2022-2023 by G. Weirich
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

package ch.elexis.ungrad.lucinda.view;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.StorageController;
import ch.elexis.ungrad.common.ui.MailUI;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.controller.DirectoryLabelProvider;
import ch.elexis.ungrad.lucinda.controller.DocumentComparator;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

/**
 * Show the documents folder
 * 
 * @author gerry
 *
 */
public class DirectoryViewPane extends Composite {
	private static int COLUMN_DATE = 0;
	private static int COLUMN_NAME = 1;
	private static String[] columnTitles = { "Datum", "Dateiname" };
	private int[] columnWidths = { 80, 300 };
	private Table table;
	private TableViewer tv;
	private DirectoryContentProvider dcp = new DirectoryContentProvider();
	private StorageController sc = new StorageController();

	public DirectoryViewPane(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		tv = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
		table = tv.getTable();
		createColumns();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tv.setLabelProvider(new DirectoryLabelProvider());
		tv.setContentProvider(dcp);
		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					File selected = (File) sel.getFirstElement();
					String pname = selected.getAbsolutePath();
					Program proggie = Program.findProgram(FileTool.getExtension(pname).toLowerCase());
					if (proggie != null) {
						proggie.execute(pname);
					}
				}

			}
		});
		Menu menu = new Menu(table);
		MenuItem mEdit = new MenuItem(menu, SWT.NONE);
		MenuItem mSend = new MenuItem(menu, SWT.NONE);
		mEdit.setText("Umbenennen..");
		mSend.setText("Per Mail senden");
		table.setMenu(menu);
		mEdit.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = tv.getStructuredSelection();
				if (!sel.isEmpty()) {
					File selected = (File) sel.getFirstElement();
					InputDialog id = new InputDialog(getShell(), "Dateinamen ändern",
							"Bitte geben Sie den neuen Dateinamen ein", selected.getName(), null);
					if (id.open() == Dialog.OK) {
						File dest = new File(selected.getParent(), id.getValue());
						selected.renameTo(dest);
						tv.refresh();
						tv.setSelection(new StructuredSelection(dest));
					}
				}
			}

		});
		mSend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = tv.getStructuredSelection();
				if (!sel.isEmpty()) {
					File selected = (File) sel.getFirstElement();
					String subject = CoreHub.localCfg.get(Preferences.DEFAULT_MAILSUBJECT, "Dokumente");
					String body = CoreHub.localCfg.get(Preferences.DEFAULT_MAILBODY, "Bitte beachten Sie den Anhang");
					MailUI mailer = new MailUI(getShell());
					mailer.sendMail(subject, body, "", selected.getAbsolutePath());
				}
			}
		});

	}

	public void setPatient(Patient pat) {
		File dir;
		try {
			dir = sc.getOutputDirFor(pat, true);
			tv.setInput(dir);
		} catch (Exception e) {
			ExHandler.handle(e);
			SWTHelper.showError("Can't set Patient", e.getMessage());
		}

	}

	private void createColumns() {
		for (int i = 0; i < columnTitles.length; i++) {
			TableViewerColumn tvc = new TableViewerColumn(tv, SWT.NULL);
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
				tv.setComparator(
						new DocumentComparator(index, bDirec, tc.getText().equals(Messages.Master_col_caption_date)));
				tc.setData("direction", bDirec); //$NON-NLS-1$
			}

		});
	}

	private static class DirectoryContentProvider implements IStructuredContentProvider {
		File dir;

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			dir = (File) newInput;

		}

		@Override
		public Object[] getElements(Object arg0) {
			if (dir == null) {
				return new Object[0];
			}
			return dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					String ext = FileTool.getExtension(name).toLowerCase();
					return ext.equals("pdf") || ext.equals("jpg") || ext.equals("png") || ext.equals("txt")
							|| ext.equals("odt") || ext.startsWith("doc");
				}
			});
		}

	}

}

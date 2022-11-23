package ch.elexis.ungrad.lucinda.view;

import java.io.File;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.ungrad.lucinda.controller.DocumentSorter;

public class DirectoryViewPane extends Composite {
	private static int COLUMN_DATE = 0;
	private static int COLUMN_NAME = 1;
	private static String[] columnTitles = { "Datum", "Dateiname" };
	private int[] columnWidths = { 50, 300 };
	private Table table;
	private TableViewer tv;
	private DirectoryContentProvider dcp=new DirectoryContentProvider();

	public DirectoryViewPane(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		tv = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
		table = tv.getTable();
		createColumns();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tv.setContentProvider(dcp);
		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					// gvp.loadDocument(sel.getFirstElement());
				}

			}
		});


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
				tv.setSorter(new DocumentSorter(index, bDirec, tc.getText().equals(Messages.Master_col_caption_date)));
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
			return dir.listFiles();
		}

	}


}

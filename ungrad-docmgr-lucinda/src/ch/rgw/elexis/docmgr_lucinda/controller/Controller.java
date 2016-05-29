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

package ch.rgw.elexis.docmgr_lucinda.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.omnivore.data.DocHandle;
import ch.rgw.elexis.docmgr_lucinda.Activator;
import ch.rgw.elexis.docmgr_lucinda.Preferences;
import ch.rgw.elexis.docmgr_lucinda.model.Document;
import ch.rgw.elexis.docmgr_lucinda.view.GlobalViewPane;
import ch.rgw.elexis.docmgr_lucinda.view.Master;
import ch.rgw.io.FileTool;
import ch.rgw.lucinda.Client;
import ch.rgw.lucinda.Handler;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * Controlle for the Lucinda View
 * 
 * @author gerry
 *
 */
public class Controller implements Handler, IProgressController {
	Client lucinda;
	GlobalViewPane view;
	ContentProvider cnt;
	TableViewer viewer;
	boolean bRestrictCurrentPatient = false;
	Map<Long, Integer> visibleProcesses = new HashMap<Long, Integer>();
	long actMax;
	int div;
	int actValue;
	private DocumentFilter docFilter = new DocumentFilter();

	public Controller() {
		lucinda = Activator.getDefault().getLucinda();
		Activator.getDefault().addHandler(this);
		bRestrictCurrentPatient = Boolean
				.parseBoolean(Preferences.get(Preferences.RESTRICT_CURRENT, Boolean.toString(false)));
		cnt = new ContentProvider();
		Activator.getDefault().setProgressController(this);
	}

	public void reconnect() {
		clear();
		view.setConnected(false);
		Activator.getDefault().disconnect();
		Activator.getDefault().connect();
	}

	public Composite createView(Composite parent) {
		if (Preferences.cfg.get(Preferences.SHOW_CONS, true)) {
			docFilter.add(Preferences.KONSULTATION_NAME);
		}
		if (Preferences.cfg.get(Preferences.SHOW_OMNIVORE, true)) {
			docFilter.add(Preferences.OMNIVORE_NAME);
		}
		if (Preferences.cfg.get(Preferences.SHOW_INBOX, true)) {
			docFilter.add(Preferences.INBOX_NAME);
		}

		view = new GlobalViewPane(parent, this);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					Document doc = new Document(sel.getFirstElement());
					if (doc.get(Preferences.FLD_LUCINDA_DOCTYPE).equals(Preferences.KONSULTATION_NAME)) {
						Konsultation kons = Konsultation.load(doc.get(Preferences.FLD_ID));
						if (kons.exists()) {
							ElexisEventDispatcher.fireSelectionEvent(kons);
						}
					}
				}

			}
		});
		return view;
	}

	public IStructuredContentProvider getContentProvider(TableViewer tv) {
		viewer = tv;
		tv.addFilter(docFilter);
		return cnt;

	}

	public LabelProvider getLabelProvider() {
		return new LucindaLabelProvider();
	}

	@SuppressWarnings("rawtypes")
	public void clear() {
		viewer.setInput(new ArrayList());
	}

	int cPatWidth = 0;

	public void restrictToCurrentPatient(boolean bRestrict) {
		bRestrictCurrentPatient = bRestrict;
		TableColumn tc = viewer.getTable().getColumn(Master.COLUMN_NAME);
		if (bRestrict) {
			cPatWidth = tc.getWidth();
			tc.setWidth(0);
		} else {
			tc.setWidth(cPatWidth > 0 ? cPatWidth : 100);
		}
		runQuery(view.getSearchField().getText());
	}

	public void reload() {
		runQuery(view.getSearchField().getText());
	}

	/**
	 * Send a query to the lucinda server.
	 * 
	 * @param input
	 *            Query String
	 */
	public void runQuery(String input) {
		StringBuilder query = new StringBuilder();
		if (bRestrictCurrentPatient) {
			Patient pat = ElexisEventDispatcher.getSelectedPatient();
			query.append(" +lastname:").append(pat.getName()).append(" +firstname:").append(pat.getVorname()) //$NON-NLS-1$ //$NON-NLS-2$
					.append(" +birthdate:").append(new TimeTool(pat.getGeburtsdatum()).toString(TimeTool.DATE_COMPACT)) //$NON-NLS-1$
					.append(" +").append(input); //$NON-NLS-1$
		} else {
			query.append(input);
		}
		lucinda.query(query.toString(), result -> {
			if (result.get("status").equals("ok")) { //$NON-NLS-1$ //$NON-NLS-2$
				@SuppressWarnings("rawtypes")
				List queryResult = (List) result.get("result"); //$NON-NLS-1$
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						viewer.setInput(queryResult);

					}

				});

			} else {
				Activator.getDefault().addMessage(new Document(result));
			}
		});

	}

	public void loadDocument(final Document doc) {
		String doctype = doc.get(Preferences.FLD_LUCINDA_DOCTYPE);

		if (doctype.equalsIgnoreCase(Preferences.INBOX_NAME)) {
			lucinda.get(doc.get(Preferences.FLD_ID), result -> {
				if (result.get("status").equals("ok")) { //$NON-NLS-1$ //$NON-NLS-2$
					@SuppressWarnings("unused")
					byte[] contents = (byte[]) result.get("result"); //$NON-NLS-1$
					String ext = FileTool.getExtension(doc.get("url")); //$NON-NLS-1$
					launchViewerForDocument(contents, ext);
				}
			});
		} else if (doctype.equalsIgnoreCase(Preferences.KONSULTATION_NAME)) {
			Konsultation kons = Konsultation.load(doc.get(Preferences.FLD_ID));
			if (kons.exists()) {
				String entry = kons.getEintrag().getHead();
				launchViewerForDocument(entry.getBytes(), "txt"); //$NON-NLS-1$
			} else {
				SWTHelper.showError(Messages.Controller_cons_not_found_caption, MessageFormat.format(
						Messages.Controller_cons_not_found_text, doc.get("title"))); //$NON-NLS-2$
			}
		} else if (doctype.equalsIgnoreCase(Preferences.OMNIVORE_NAME)) {
			DocHandle dh = DocHandle.load(doc.get(Preferences.FLD_ID));
			if (dh.exists()) {
				dh.execute();
			} else {
				SWTHelper.showError(Messages.Controller_omnivore_not_found_caption,
						Messages.Controller_omnivore_not_found_text,
						doc.get("title")); //$NON-NLS-1$
			}
		} else {

			SWTHelper.showError(Messages.Controller_unknown_type_caption,
					MessageFormat.format(Messages.Controller_unknown_type_text, doctype));
		}

	}

	@Override
	public void signal(Map<String, Object> message) {
		switch ((String) message.get("status")) { //$NON-NLS-1$
		case "connected": //$NON-NLS-1$
			view.setConnected(true);
			break;
		case "disconnected": //$NON-NLS-1$
			view.setConnected(false);
			break;
		}
	}

	public void launchViewerForDocument(byte[] cnt, String ext) {
		try {
			File temp = File.createTempFile("_lucinda_", "_." + ext); //$NON-NLS-1$ //$NON-NLS-2$
			temp.deleteOnExit();
			FileTool.writeFile(temp, cnt);
			Program proggie = Program.findProgram(ext);
			if (proggie != null) {
				proggie.execute(temp.getAbsolutePath());
			} else {
				if (Program.launch(temp.getAbsolutePath()) == false) {
					Runtime.getRuntime().exec(temp.getAbsolutePath());
				}
			}

		} catch (Exception ex) {
			ExHandler.handle(ex);
			SWTHelper.showError(Messages.Controller_could_not_launch_file, ex.getMessage());
		}
	}

	/**
	 * Display a progress bar at the bottom of the lucinda view, or add a new
	 * process to an existing process bar. If more than one process wants to
	 * dosplay, the values for all processes are added and the sum ist the upper
	 * border of the progress bar.
	 * 
	 * @param maximum
	 *            the value to reach
	 * @return a Handle to use for later addProgrss Calls
	 * @see addProgress
	 */
	public Long initProgress(int maximum) {
		Long proc = System.currentTimeMillis() + new Random().nextLong();
		visibleProcesses.put(proc, maximum);
		long val = 0;
		for (Integer k : visibleProcesses.values()) {
			val += k;
		}
		if (val < Integer.MAX_VALUE) {
			div = 1;
		}
		int amount = (int) (val / div);
		view.initProgress(amount);

		return proc;
	}

	/**
	 * show progress.
	 * 
	 * @param the
	 *            Handle as received from initProgress
	 * @param amount
	 *            the amount of work done since the last call. I the accumulated
	 *            amount of all calls to addProgress is higher than the maximum
	 *            value, the progress bar is hidden.
	 * 
	 */
	public void addProgress(Long handle, int amount) {
		Integer val = visibleProcesses.get(handle);
		val -= amount;
		if (val <= 0) {
			visibleProcesses.remove(handle);
			amount += val;
			if (visibleProcesses.isEmpty()) {
				view.finishProgress();
			}
		} else {
			visibleProcesses.put(handle, val);
		}
		actValue += amount;
		view.showProgress(actValue / div);

	}

	/**
	 * Doctype filter
	 * 
	 * @param bOn
	 *            whether the doctype should be filtered or not
	 * @param doctype
	 *            the doctype to filter (lucinda_doctype)
	 */
	public void toggleDoctypeFilter(boolean bOn, String doctype) {
		if (bOn) {
			docFilter.add(doctype);
		} else {
			docFilter.remove(doctype);
		}
		viewer.refresh();
	}

	public void changePatient(Patient object) {
		if (bRestrictCurrentPatient) {
			Text text = view.getSearchField();
			String q = text.getText();
			if (q.isEmpty()) {
				text.setText("*:*"); //$NON-NLS-1$
			}
			runQuery(q);
		}
	}

	public void setColumnWidths(String widths) {
		TableColumn[] tcs = viewer.getTable().getColumns();
		String[] cw = widths.split(","); //$NON-NLS-1$
		if (cw.length == tcs.length) {
			for (int i = 0; i < cw.length; i++) {
				try {
					int w = Integer.parseInt(cw[i]);
					tcs[i].setWidth(w);
				} catch (NumberFormatException nex) {
					// do nothing
				}
			}
		}
	}

	public String getColumnWidths() {
		TableColumn[] tcs = viewer.getTable().getColumns();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tcs.length; i++) {
			sb.append(Integer.toString(tcs[i].getWidth())).append(","); //$NON-NLS-1$
		}
		return sb.substring(0, sb.length() - 1);
	}

}

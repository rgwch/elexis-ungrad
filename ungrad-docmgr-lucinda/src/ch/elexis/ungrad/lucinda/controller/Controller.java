/*******************************************************************************
 * Copyright (c) 2016-2022 by G. Weirich
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

package ch.elexis.ungrad.lucinda.controller;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.lucinda.Activator;
import ch.elexis.ungrad.lucinda.Lucinda;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.view.DirectoryViewPane;
import ch.elexis.ungrad.lucinda.view.GlobalViewPane;
import ch.elexis.ungrad.lucinda.view.Master;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;;

/**
 * Controller for the Lucinda View
 * 
 * @author gerry
 *
 */
public class Controller implements IProgressController {
	GlobalViewPane lucindaView;
	DirectoryViewPane dirView;
	ContentProvider cnt;
	TableViewer viewer;
	boolean bRestrictCurrentPatient = false;
	Map<Long, Integer> visibleProcesses = new HashMap<Long, Integer>();
	long actMax;
	int div;
	int actValue;
	private Lucinda lucinda;
	private Set<String> allowed_doctypes = new TreeSet<>();
	private Logger log = LoggerFactory.getLogger(Controller.class);
	Composite envelope;
	StackLayout stack = new StackLayout();

	public Controller() {
		lucinda = new Lucinda();
		bRestrictCurrentPatient = Boolean
				.parseBoolean(Preferences.get(Preferences.RESTRICT_CURRENT, Boolean.toString(false)));
		cnt = new ContentProvider();
	}

	public Composite createView(Composite parent) {
		if (Preferences.cfg.get(Preferences.SHOW_CONS, true)) {
			allowed_doctypes.add(Preferences.KONSULTATION_NAME);
		}
		if (Preferences.cfg.get(Preferences.SHOW_OMNIVORE, true)) {
			allowed_doctypes.add(Preferences.OMNIVORE_NAME);
		}
		if (Preferences.cfg.get(Preferences.SHOW_INBOX, true)) {
			allowed_doctypes.add(Preferences.INBOX_NAME);
		}

		envelope = new Composite(parent, SWT.NONE);
		envelope.setLayout(stack);
		lucindaView = new GlobalViewPane(envelope, this);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					@SuppressWarnings("unchecked")
					Map<String, Object> doc = (Map<String, Object>) sel.getFirstElement();
					String doctype = (String) doc.get(Preferences.FLD_LUCINDA_DOCTYPE);
					if (Preferences.KONSULTATION_NAME.equals(doctype)) {
						Konsultation kons = Konsultation.load((String) doc.get(Preferences.FLD_ID));
						if (kons.exists()) {
							ElexisEventDispatcher.fireSelectionEvent(kons);
						}
					}
				}

			}
		});
		dirView = new DirectoryViewPane(envelope);
		if (CoreHub.localCfg.get(Preferences.COMMON_DIRECTORY, false) == true) {
			stack.topControl = dirView;
		} else {
			stack.topControl = lucindaView;
		}
		changePatient(ElexisEventDispatcher.getSelectedPatient());
		return envelope;
	}

	public void setDirView() {
		stack.topControl = dirView;
		envelope.layout();
	}

	public void setLucindaView() {
		stack.topControl = lucindaView;
		envelope.layout();
	}

	public IStructuredContentProvider getContentProvider(TableViewer tv) {
		viewer = tv;
		// tv.addFilter(docFilter);
		return cnt;

	}

	public LabelProvider getLabelProvider() {
		return new LucindaLabelProvider();
	}

	public void clear() {
		viewer.setInput(new ArrayList<Map<String, Object>>());
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
		runQuery(lucindaView.getSearchField().getText());
	}

	public void reload() {
		dirView.setPatient(ElexisEventDispatcher.getSelectedPatient());
		runQuery(lucindaView.getSearchField().getText());
	}

	public void doRescan() {
		lucinda.rescan();
	}

	/**
	 * Send a query to the lucinda server.
	 * 
	 * @param input Query String
	 * @throws Exception
	 */
	public void runQuery(String input) {
		try {
			/*
			 * if(!input.matches("[\\:\\(\\)]")) { input="contents:"+input; }
			 */
			Map result = lucinda.query(buildQuery(input));
			String status = (String) result.get("status");

			if ("ok".equals(status)) { //$NON-NLS-1$ //$NON-NLS-2$
				List<Map> queryResult = (List) result.get("result"); //$NON-NLS-1$

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						viewer.setInput(queryResult);
					}
				});
			} else {
				Activator.getDefault().addMessage(result);
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
	}

	/*
	 * Compose a query from the user's supplied query string and the query refiners,
	 * such as doctypes and restrict to Patient.
	 * 
	 * @param input The user supplied query
	 * 
	 * @return the refined query
	 */
	protected String buildQuery(String input) {
		StringBuilder q = new StringBuilder();
		if (bRestrictCurrentPatient) {
			Patient pat = ElexisEventDispatcher.getSelectedPatient();
			if (pat != null) {
				q.append("+concern:").append(pat.getName().replaceAll(" ", "_")).append("_")
						.append(pat.getVorname().replaceAll(" ", "_")).append("_")
						.append(new TimeTool(pat.getGeburtsdatum()).toString((TimeTool.DATE_GER)));
				/*
				 * q.append("+lastname:").append(pat.getName()).append(" +firstname:")
				 * //$NON-NLS-1$//$NON-NLS-2$ .append(pat.getVorname()).append(" +birthdate:")
				 * //$NON-NLS-1$ .append(new
				 * TimeTool(pat.getGeburtsdatum()).toString(TimeTool.DATE_COMPACT));
				 */
			}
		}

		if (allowed_doctypes.isEmpty()) {
			q.append(" -lucinda_doctype:*"); //$NON-NLS-1$
		} else {
			q.append(" +("); //$NON-NLS-1$
			for (String doctype : allowed_doctypes) {
				q.append(" lucinda_doctype:").append(doctype); //$NON-NLS-1$
			}
			q.append(")");//$NON-NLS-1$
		}
		if (StringTool.isNothing(input) || input.equals("*") || input.equals("*:*")) {
			q.append(" contents:*");
		} else if (input.contains(":")) {
			q.append("+").append(input);
		} else {
			q.append("+").append("contents:(").append(input).append(")");
		}
		log.info(q.toString());
		return q.toString();
	}

	/**
	 * fetch the contents of a document and launch an associated application if it's
	 * a file from the Lucinda inbox, launch it from there.
	 * 
	 * @param doc
	 */
	public void loadDocument(final Map doc) {
		String doctype = (String) doc.get(Preferences.FLD_LUCINDA_DOCTYPE);

		if (Preferences.INBOX_NAME.equalsIgnoreCase(doctype)) {
			String docbase = Preferences.cfg.get(Preferences.DOCUMENT_STORE, "");
			loadFile(docbase, doc);

		} else if (doctype.equalsIgnoreCase(Preferences.KONSULTATION_NAME)) {
			Konsultation kons = Konsultation.load((String) doc.get(Preferences.FLD_ID));
			if (kons.exists()) {
				String entry = kons.getEintrag().getHead();
				if (entry.startsWith("<")) {
					Samdas samdas = new Samdas(entry);
					entry = samdas.getRecordText();
				}
				// launchViewerForDocument(entry.getBytes(), "txt"); //$NON-NLS-1$
				Fall fall = kons.getFall();
				Patient pat = fall.getPatient();
				ElexisEventDispatcher.fireSelectionEvents(pat, fall, kons);
			} else {
				SWTHelper.showError(Messages.Controller_cons_not_found_caption,
						MessageFormat.format(Messages.Controller_cons_not_found_text, doc.get("title"))); // $NON-NLS-2$
			}
		} else if (doctype.equalsIgnoreCase(Preferences.OMNIVORE_NAME)) {
			DocHandle dh = DocHandle.load((String) doc.get(Preferences.FLD_ID));
			if (dh.exists()) {
				dh.execute();
			} else {
				SWTHelper.showError(Messages.Controller_omnivore_not_found_caption,
						Messages.Controller_omnivore_not_found_text, (String) doc.get("title")); //$NON-NLS-1$
			}
		} else {

			SWTHelper.showError(Messages.Controller_unknown_type_caption,
					MessageFormat.format(Messages.Controller_unknown_type_text, doctype));
		}

	}

	private void loadFile(String docbase, Map<String, Object> doc) {
		final String loc = (String) doc.get(Preferences.FLD_LOCATION);
		if (loc.contains(":/") || StringTool.isNothing(docbase)) {
			try {
				Map result = lucinda.get((String) doc.get(Preferences.FLD_ID));
				if (result.get("status").equals("ok")) { //$NON-NLS-1$ //$NON-NLS-2$
					byte[] contents = (byte[]) result.get("result"); //$NON-NLS-1$
					String ext = FileTool.getExtension(loc); // $NON-NLS-1$
					File temp = File.createTempFile("lucinda_", "." + ext); //$NON-NLS-1$ //$NON-NLS-2$
					temp.deleteOnExit();
					FileTool.writeFile(temp, contents);
					asyncRunViewer(temp.getAbsolutePath());
				} else {
					throw (new Exception("Could not fetch file contents " + result.get("message")));
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
				SWTHelper.showError(Messages.Controller_could_not_launch_file, ex.getMessage());

			}

		} else {
			if (!loc.startsWith("/") && !loc.contains(":\\")) {
				if (!docbase.endsWith(File.separator)) {
					docbase += File.separator;
				}
				asyncRunViewer(docbase + loc);
			} else {
				asyncRunViewer(loc);
			}
		}
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
					SWTHelper.showError(Messages.Controller_could_not_launch_file, ex.getMessage());
				}
			}

		});

	}

	/**
	 * Launch a script to acquire a document from the scanner
	 * 
	 * @param shell
	 */
	public void launchAquireScript(String command, Shell shell) {
		try {
			Patient pat = ElexisEventDispatcher.getSelectedPatient();
			if (pat == null) {
				throw new Exception("No patient selected");
			} else {
				String[] name = pat.getName().split("[ -]+");
				String[] fname = pat.getVorname().split("[ -]+");
				TimeTool bdate = new TimeTool(pat.getGeburtsdatum());
				StringBuilder sbConcern = new StringBuilder();
				String bdatec = bdate.toString(TimeTool.DATE_COMPACT);
				sbConcern.append(name[0]).append("_").append(fname[0]).append("_").append(bdatec.substring(6, 8))
						.append(".").append(bdatec.substring(4, 6)).append(".").append(bdatec.substring(0, 4));
				InputDialog id = new InputDialog(shell, "Document title", "Please enter a title for the document", "",
						null);
				if (id.open() == Dialog.OK) {
					String title = id.getValue().replaceAll("[\\/\\:\\-\\?\\+\\*<>, ]", "_");
					new ScanJob(command, sbConcern.toString(), title).schedule();
				}

			}

		} catch (Exception ex) {
			ExHandler.handle(ex);
			SWTHelper.showError("Could not launch script", ex.getMessage());
		}
	}

	class ScanJob extends Job {
		String command;
		String concern;
		String title;

		public ScanJob(String command, String concern, String title) {
			super("Acquire document");
			this.command = command;
			this.concern = concern;
			this.title = title;
		}

		@Override
		protected IStatus run(IProgressMonitor arg0) {
			try {
				Process proc = Runtime.getRuntime().exec(new String[] { command, concern, title });
				int result = proc.waitFor();
				if (result != 0) {
					log.error("could not launch aquire script");
					// SWTHelper.showError("Scan Script", "Fehler bei der Ausführung");
					return new Status(Status.ERROR, "Acquire", "Could not execute");
				}
				return Status.OK_STATUS;
			} catch (Exception ex) {
				// SWTHelper.showError("Dokument Einlesen", ex.getMessage());
				return new Status(Status.ERROR, "Acquire", ex.getMessage());

			}
		}

	}

	/**
	 * Display a progress bar at the bottom of the lucinda view, or add a new
	 * process to an existing process bar. If more than one process wants to
	 * display, the values for all processes are added and the sum is the upper
	 * border of the progress bar.
	 * 
	 * @param maximum the value to reach
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
		lucindaView.initProgress(amount);

		return proc;
	}

	/**
	 * show progress.
	 * 
	 * @param the    Handle as received from initProgress
	 * @param amount the amount of work done since the last call. I the accumulated
	 *               amount of all calls to addProgress is higher than the maximum
	 *               value, the progress bar is hidden.
	 * 
	 */
	public void addProgress(Long handle, int amount) {
		Integer val = visibleProcesses.get(handle);
		val -= amount;
		if (val <= 0) {
			visibleProcesses.remove(handle);
			amount += val;
			if (visibleProcesses.isEmpty()) {
				lucindaView.finishProgress();
				actValue = 0;
			}
		} else {
			visibleProcesses.put(handle, val);
			actValue += amount;
			lucindaView.showProgress(actValue / div);
		}
	}

	/**
	 * Doctype filter
	 * 
	 * @param bOn     whether the doctype should be filtered or not
	 * @param doctype the doctype to filter (lucinda_doctype)
	 */
	public void toggleDoctypeFilter(boolean bOn, String doctype) {
		if (bOn) {
			allowed_doctypes.add(doctype);
		} else {
			allowed_doctypes.remove(doctype);
		}
		reload();
	}

	public void changePatient(Patient object) {
		if (bRestrictCurrentPatient) {
			Text text = lucindaView.getSearchField();
			String q = text.getText();
			runQuery(q);
		}
		dirView.setPatient(object);
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

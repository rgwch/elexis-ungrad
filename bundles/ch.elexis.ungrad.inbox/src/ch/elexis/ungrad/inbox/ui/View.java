/*******************************************************************************
 * Copyright (c) 2023-2026, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.inbox.ui;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.ungrad.IMAPMail;
import ch.elexis.ungrad.MBox;
import ch.elexis.ungrad.inbox.model.Controller;
import ch.elexis.ungrad.inbox.model.DocumentDescriptor;
import ch.elexis.ungrad.inbox.model.FilenameMatcher;
import ch.elexis.ungrad.inbox.model.PreferenceConstants;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * The Main View of Ungrad Inbox
 * 
 * @author gerry
 *
 */
public class View extends ViewPart {
	TableViewer tv;
	Controller controller = new Controller();
	FilenameMatcher fmatch = new FilenameMatcher();
	private IAction addAction, deleteAction, execAction, reloadAction, loadMailAction;
	private String[] whitelist;

	public View() {
		String senders = CoreHub.localCfg.get(PreferenceConstants.WHITELIST, "");
		if (StringTool.isNothing(senders)) {
			whitelist = new String[0];
		} else {
			whitelist = senders.split("\\n");
		}

	}

	@Override
	public void createPartControl(Composite parent) {
		makeActions();
		tv = new TableViewer(parent);
		tv.setLabelProvider(controller);
		tv.setContentProvider(controller);
		tv.setComparator(new ViewerComparator() {

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return (controller.getColumnText(e1, 0).compareTo(controller.getColumnText(e2, 0)));
			}

		});

		tv.getControl().setLayoutData(SWTHelper.getFillGridData());
		tv.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				addAction.setEnabled(!sel.isEmpty());
				deleteAction.setEnabled(!sel.isEmpty());
				execAction.setEnabled(!sel.isEmpty());
			}
		});
		tv.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				launchViewer(getSelection());
			}
		});
		ViewMenus menus = new ViewMenus(getViewSite());
		menus.createToolbar(addAction, execAction, loadMailAction, reloadAction, null, deleteAction);

		addAction.setEnabled(false);
		deleteAction.setEnabled(false);
		execAction.setEnabled(false);
		if (CoreHub.localCfg.get(PreferenceConstants.MAILMODE, "none").equals("none")) {
			loadMailAction.setEnabled(false);
		}
		tv.setInput(CoreHub.localCfg.get(PreferenceConstants.BASEDIR, ""));
	}

	@Override
	public void setFocus() {
		reload();
	}

	public File getSelection() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		return (File) sel.getFirstElement();
	}

	private void reload() {
		UiDesk.asyncExec(new Runnable() {
			@Override
			public void run() {
				tv.refresh();
			}
		});
	}

	/**
	 * Display the currently selected file with its associated viewer
	 * 
	 * @param sel
	 */
	void launchViewer(File sel) {
		try {
			String ext = FileTool.getExtension(sel.getName());
			Program proggie = Program.findProgram(ext);
			String arg = sel.getAbsolutePath();
			if (proggie != null) {
				proggie.execute(arg);
			} else {
				if (Program.launch(sel.getAbsolutePath()) == false) {
					Runtime.getRuntime().exec(arg);
				}

			}

		} catch (Exception ex) {
			ExHandler.handle(ex);
			SWTHelper.showError(Messages.View_Error_CouldNotStart, ex.getMessage());
		}
	}

	private void makeActions() {
		/**
		 * Fetch Mail attachments into inbox (If mail receive is configured)
		 */
		loadMailAction = new Action(Messages.View_Action_FetchMail_Title) {
			{
				setToolTipText(Messages.View_Action_FetchMail_Tooltip);
				setImageDescriptor(Images.IMG_MAIL.getImageDescriptor());
			}
			private IMAPMail.INotifier notifier = new IMAPMail.INotifier() {
				File dir = new File(CoreHub.localCfg.get(PreferenceConstants.BASEDIR, ""));

				@Override
				public void documentFound(String name, byte[] doc, String sender, String subject) {
					try {
						FileTool.writeFile(new File(dir, name), doc);
						FileTool.writeTextFile(new File(dir, name + ".meta"), sender + "," + subject);
						reload();
					} catch (Exception ex) {
						SWTHelper.alert(Messages.View_Error_Writing,
								MessageFormat.format(Messages.View_Error_Writing, name));
					}

				}
			};

			@Override
			public void run() {
				try {
					String mailMode = CoreHub.localCfg.get(PreferenceConstants.MAILMODE, "none");
					if (mailMode.equals("mbox")) {
						String mbox = CoreHub.localCfg.get(PreferenceConstants.MBOX, "");
						new MBox(mbox, whitelist).readMessages(notifier);
					} else if (mailMode.equals("imap")) {
						new IMAPMail(whitelist, notifier).fetch();
					}

				} catch (Exception e) {
					SWTHelper.alert(Messages.View_Error_FetchingMail, e.getLocalizedMessage());
					ExHandler.handle(e);
				}
			}
		};

		/**
		 * Associate selected file with patient and optional change filename
		 */
		addAction = new Action(Messages.View_Action_Assign_Title) {
			{
				setToolTipText(Messages.View_Action_Assign_Tooltip);
				setImageDescriptor(Images.IMG_IMPORT.getImageDescriptor());
			}

			@Override
			public void run() {
				try {
					File sel = getSelection();
					if (sel != null) {
						DocumentDescriptor dd = fmatch.analyze(sel);
						ImportDocumentDialog idlg = new ImportDocumentDialog(View.this, dd);
						if (idlg.open() == Dialog.OK) {
							// System.out.print(idlg.getValue());
							if (dd.concerns()) {
								// boolean bUseKI = CoreHub.localCfg.get(PreferenceConstants.USE_AI, false);
								controller.moveFileToDocbase(dd.concerns_id, sel, idlg.getValue(), idlg.bUseKI);
								reload();
							} else {
								SWTHelper.alert(Messages.ImportDocumentDialog_ErrorNoPatient,
										Messages.ImportDocumentDialog_ErrorNoPatient);
							}
						}
					}
				} catch (Exception ex) {
					SWTHelper.showError(Messages.View_Error_Moving, ex.getMessage());
				}
			}
		};

		/**
		 * Delete currently selected file
		 */
		deleteAction = new Action(Messages.View_Action_Delete_Title) {
			{
				setToolTipText(Messages.View_Action_Delete_Tooltip);
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
			}

			@Override
			public void run() {
				File sel = getSelection();
				if (SWTHelper.askYesNo(Messages.Plugin_PreferencesName,
						MessageFormat.format("{0} wirklich löschen?", sel.getName()))) {
					File meta = new File(sel.getAbsolutePath() + ".meta");
					if (meta.exists()) {
						meta.delete();
					}
					sel.delete();
					reload();
				}
			}
		};

		/**
		 * Display currently selected file
		 */
		execAction = new Action(Messages.View_Action_Open_Title) {
			{
				setToolTipText(Messages.View_Action_Open_Tooltip);
				setImageDescriptor(Images.IMG_EYE_WO_SHADOW.getImageDescriptor());
			}

			@Override
			public void run() {
				launchViewer(getSelection());
			}
		};

		/**
		 * Refresh contents of the inbox view
		 * 
		 */
		reloadAction = new Action(Messages.View_Action_Reload_Title) {
			{
				setToolTipText(Messages.View_Action_Reload_Tooltip);
				setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
			}

			@Override
			public void run() {
				reload();
			}
		};
	}

}

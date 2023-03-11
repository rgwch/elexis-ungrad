package ch.elexis.ungrad.inbox.ui;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
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
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.services.GlobalServiceDescriptors;
import ch.elexis.core.data.services.IDocumentManager;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.inbox.model.Controller;
import ch.elexis.ungrad.inbox.model.PreferenceConstants;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public class View extends ViewPart {
	TableViewer tv;
	Controller controller = new Controller();
	private IAction addAction, deleteAction, execAction, reloadAction;

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
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				addAction.setEnabled(!sel.isEmpty());
				deleteAction.setEnabled(!sel.isEmpty());
				execAction.setEnabled(!sel.isEmpty());
			}
		});

		ViewMenus menus = new ViewMenus(getViewSite());
		menus.createToolbar(addAction, execAction, reloadAction, null, deleteAction);
		addAction.setEnabled(false);
		deleteAction.setEnabled(false);
		execAction.setEnabled(false);
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

	private void makeActions() {
		addAction = new Action("Zuweisen") {
			{
				setToolTipText("Dokument zuweisen");
				setImageDescriptor(Images.IMG_OK.getImageDescriptor());
			}

			@Override
			public void run() {
				File sel = getSelection();
				Patient pat = ElexisEventDispatcher.getSelectedPatient();
				if (sel != null && pat != null) {
					
					InputDialog idlg=new InputDialog(getSite().getShell(), "Datei Speichern als", pat.getLabel(), sel.getName(), null);
					if(idlg.open()==Dialog.OK) {
						System.out.print(idlg.getValue());
					}
				}
			}
		};
		deleteAction = new Action("Löschen") {
			{
				setToolTipText("Dokument löschen");
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
			}

			@Override
			public void run() {
				File sel = getSelection();
				if (SWTHelper.askYesNo("Eingangsfach", MessageFormat.format("{0} wirklich löschen?", sel.getName()))) {
					sel.delete();
					reload();
				}
			}
		};

		execAction = new Action("Öffnen") {
			{
				setToolTipText("Dokument öffnen");
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
			}

			@Override
			public void run() {
				try {
					File sel = getSelection();
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
					SWTHelper.showError("Konnte nicht starten", ex.getMessage());
				}
			}
		};
		reloadAction = new Action("Neu einlesen") {
			{
				setToolTipText("Eingangsfach neu einlesen");
				setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
			}

			@Override
			public void run() {
				reload();
			}
		};
	}

}

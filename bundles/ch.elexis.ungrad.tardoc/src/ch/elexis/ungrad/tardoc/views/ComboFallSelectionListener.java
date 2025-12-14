package ch.elexis.ungrad.tardoc.views;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.Messages;
import ch.rgw.tools.Result;

public class ComboFallSelectionListener implements ISelectionChangedListener {
	private boolean ignoreEventSelectionChanged;
	private TardocKonsView tkv;

	public ComboFallSelectionListener(TardocKonsView view) {
		this.ignoreEventSelectionChanged = false;
		this.tkv = view;
	}
	public void ignoreSelectionEventOnce() {
		this.ignoreEventSelectionChanged = true;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (!ignoreEventSelectionChanged) {
			ISelection selection = event.getSelection();
			if (selection instanceof StructuredSelection) {
				if (!selection.isEmpty()) {
					ICoverage changeToCoverage = (ICoverage) ((StructuredSelection) selection).getFirstElement();

					ICoverage actCoverage = null;
					String fallLabel = "Current Case NOT found!!";//$NON-NLS-1$
					if (tkv.actEncounter != null) {
						actCoverage = tkv.actEncounter.getCoverage();
						fallLabel = actCoverage.getLabel();
					}

					if (!changeToCoverage.equals(actCoverage)) {
						if (!changeToCoverage.isOpen()) {
							SWTHelper.alert(Messages.Core_Case_is_closed, // $NON-NLS-1$
									Messages.KonsDetailView_CaseClosedBody); // $NON-NLS-1$
						} else {
							MessageDialog msd = new MessageDialog(tkv.getViewSite().getShell(),
									Messages.KonsDetailView_ChangeCaseCaption, // $NON-NLS-1$
									Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize),
									MessageFormat.format(Messages.KonsDetailView_ConfirmChangeConsToCase,
											new Object[] { fallLabel, changeToCoverage.getLabel() }),
									MessageDialog.QUESTION, new String[] { Messages.Core_Yes, // $NON-NLS-1$
											Messages.Corr_No },
									0); // $NON-NLS-1$
							if (msd.open() == Window.OK) {
								Result<IEncounter> transferResult = EncounterServiceHolder.get()
										.transferToCoverage(tkv.actEncounter, changeToCoverage, false);
								if (!transferResult.isOK()) {
									SWTHelper.alert("Error", transferResult.toString());
								}

							} else {
								ignoreSelectionEventOnce();
								tkv.casesComposite.tableComboViewerFall.setSelection(new StructuredSelection(actCoverage));
							}
						}
					}
				}
			}
		}
		ignoreEventSelectionChanged = false;
	}
}

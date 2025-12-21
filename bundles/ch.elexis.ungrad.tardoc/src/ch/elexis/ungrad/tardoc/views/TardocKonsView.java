/*******************************************************************************
 * Copyright (c) 2025 by G. Weirich
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
 * Substantial contributions:  Copilot (c) 2024 GitHub, Inc. using Claude Sonnet 4.5
 *********************************************************************************/

package ch.elexis.ungrad.tardoc.views;

import java.time.LocalDate;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.ungrad.tardoc.Messages;
import ch.elexis.ungrad.tardoc.services.BillingsManager;
import ch.elexis.ungrad.tardoc.services.EncounterTimer;
import ch.elexis.ungrad.tardoc.services.TardocManager;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;
import jakarta.inject.Inject;

/**
 * Tardoc consultation view with timer, text area, and billing positions list.
 * This is the main view of the Tardoc plugin. We started from the standard
 * Elexis KonsDetailView, so there's some leftover code and comments referring
 * to "KonsDetailView".
 */
public class TardocKonsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ch.elexis.ungrad.tardoc.views.TardocKonsView";

	Hashtable<String, IKonsExtension> hXrefs;

	private DetailsComposite cDesc;
	public TimerComposite timerComposite;
	public EncounterTimer encounterTimer = new EncounterTimer();
	CasesComposite casesComposite;

	protected IEncounter actEncounter;
	EnhancedTextField etf;
	BillingsManager billingsManager = new BillingsManager(this);

	IPatient actPat;
	private boolean created = false;

	// Text area

	// Additions composite (will contain billing positions, diagnoses, etc.)
	private AdditionsComposite additionsComposite;

	// split the view vertically between text and additions
	private SashForm sashForm;
	// If false, additions section is hidden and text is full height
	private boolean additionsVisible = true;
	private Action toggleAdditionsAction;

	// toggle between showing billing positions and diagnoses
	private Action toggleViewAction;

	// Save sash weights and additions composite state
	private IMemento memento;
	private static final int[] DEFAULT_WEIGHTS = new int[] { 60, 40 };

	// Set current encounter text when view becomes visible
	private IPartListener2 udpateOnVisible = new IPartListener2() {
		@Override
		public void partActivated(org.eclipse.ui.IWorkbenchPartReference partRef) {
			if (actEncounter != null && etf != null && !etf.isDisposed() && !etf.isDirty()) {
				setKonsText(actEncounter, actEncounter.getVersionedEntry().getHeadVersion());
			}
		}

		@Override
		public void partDeactivated(org.eclipse.ui.IWorkbenchPartReference partRef) {
			// save entry on deactivation if text was edited
			if (actEncounter != null && (etf.isDirty())) {
				EncounterServiceHolder.get().updateVersionedEntry(actEncounter, etf.getContentsAsXML(),
						getVersionRemark());
				etf.setDirty(false);
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, actEncounter);
			}
		};

	};

	@Optional
	@Inject
	void udpatePatient(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IPatient patient) {
		if (patient != null && patient.equals(actPat) && created) {
			actPat = null; // make sure patient will be updated
			setPatient(patient);
		}
	}

	@Inject
	@Optional
	public void reloadPatient(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IPatient.class.equals(clazz) && created) {
			actPat = null; // make sure patient will be updated
			setPatient(actPat);
		}
	}

	@Inject
	void activePatient(@Optional IPatient patient) {
		if (created) {
			Display.getDefault().asyncExec(() -> {
				actPat = null; // make sure patient will be updated
				setPatient(patient);
			});
		}
	}

	@Inject
	void activeUser(@Optional IUser user) {
		if (created) {
			Display.getDefault().asyncExec(() -> {
				// adaptMenus();
			});
		}
	}

	@Inject
	@Optional
	public void reloadCoverage(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (ICoverage.class.equals(clazz) && created) {
			updateFallCombo();
		}
	}

	@Inject
	void updateCoverage(@Optional @UIEventTopic(ElexisEventTopics.EVENT_UPDATE) ICoverage coverage) {
		if (created && casesComposite != null && !casesComposite.isDisposed()) {
			updateFallCombo();
		}
	}

	private String getVersionRemark() {
		String remark = "edit"; //$NON-NLS-1$
		java.util.Optional<IUser> activeUser = ContextServiceHolder.get().getRootContext().getTyped(IUser.class);
		if (activeUser.isPresent()) {
			remark = activeUser.get().getLabel();
		}
		return remark;
	}

	/** Aktuellen Patienten setzen */
	private synchronized void setPatient(IPatient pat) {
		LoggerFactory.getLogger(getClass()).info("[KONS PAT] " + (pat != null ? pat.getId() : "null"));
		actPat = pat;
		if (pat != null) {
			// stickerComposite.setPatient(pat);
			updateFallCombo();
		}

	}

	private void updateFallCombo() {
		casesComposite.refreshCases(actEncounter);
	}

	@Inject
	void selectedEncounter(@Optional IEncounter encounter) {
		if (created) {
			Display.getDefault().asyncExec(() -> {
				if (encounter != null) {
					// ElexisEvent.EVENT_SELECTED
					setKons(encounter);
				} else {
					// ElexisEvent.EVENT_DESELECTED
					setKons(null);
				}
			});
		}
	}

	// Set the current encounter and propagate to subcomponents
	private synchronized void setKons(final IEncounter encounter) {
		LoggerFactory.getLogger(getClass()).info("[KONS] " + (encounter != null ? encounter.getId() : "null"));

		if (actEncounter != null && etf.isDirty()) {
			EncounterServiceHolder.get().updateVersionedEntry(actEncounter, etf.getContentsAsXML(), getVersionRemark());
			etf.setDirty(false);
		}
		cDesc.setEncounter(encounter);
		if (encounter != null) {
			ICoverage coverage = encounter.getCoverage();
			// TODO remove, pat should be already set via context
			// setPatient(coverage.getPatient());
			setKonsText(encounter, encounter.getVersionedEntry().getHeadVersion());

			casesComposite.setEncounter(coverage);

			// TODO Save this for later: different background for today's kons
			if (encounter.getDate().isEqual(LocalDate.now())) {
				etf.setTextBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			} else {
				etf.setTextBackground(UiDesk.getColor(UiDesk.COL_WHITE)); // $NON-NLS-1$
			}
			billingsManager.setEncounter(encounter);
			// Update billing positions composite with encounter data
			if (additionsComposite != null && !additionsComposite.isDisposed()) {
				additionsComposite.getBillingPositionsComposite().setKons(encounter);
				additionsComposite.getDiagnosesComposite().setKons(encounter);
			}
			encounterTimer.stop();
			etf.setEnabled(true);
		} else {
			etf.setText(StringUtils.EMPTY);
			etf.setEnabled(false);
		}
		actEncounter = encounter;
		cDesc.layout();

	}

	/**
	 * Set the text of the encounter to the specified version of the stored
	 * VersionedResource
	 */
	void setKonsText(final IEncounter encounter, final int version) {
		String ntext = StringUtils.EMPTY;
		if ((version >= 0) && (version <= encounter.getVersionedEntry().getHeadVersion())) {
			VersionedResource vr = encounter.getVersionedEntry();
			ResourceItem entry = vr.getVersion(version);
			ntext = entry.data;
			StringBuilder sb = new StringBuilder();
			sb.append("rev. ").append(version).append(ch.elexis.core.ui.views.Messages.KonsDetailView_of) //$NON-NLS-1$
					.append( // $NON-NLS-2$
							new TimeTool(entry.timestamp).toString(TimeTool.FULL_GER))
					.append(" (").append(entry.remark).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		etf.setText(ntext);
		etf.setKons(encounter);
		// displayedVersion = version;
		// versionBackAction.setEnabled(version != 0);
		// versionFwdAction.setEnabled(version !=
		// encounter.getVersionedEntry().getHeadVersion());
	}

	/** Get the current text of the consultation as plaintext */
	public String getText() {
		return etf.getContentsPlaintext();
	}

	/**
	 * Insert text at the end of the consultation text
	 * 
	 * @param text
	 */
	public void insertTextAtEndOfKons(String text) {
		if (text == null || text.trim().isEmpty()) {
			return;
		}
		String existing = etf.getContentsPlaintext();
		if (LocalLockServiceHolder.get().acquireLock(actEncounter).isOk()) {
			etf.replace(0, existing.length(), existing + text);
			EncounterServiceHolder.get().updateVersionedEntry(actEncounter, new Samdas(etf.getContentsAsXML()));

			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, actEncounter);
			LocalLockServiceHolder.get().releaseLock(actEncounter);
		}
	}

	/**
	 * Initializes the view with the given site and memento.
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws org.eclipse.ui.PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}

	@Override
	public void createPartControl(final Composite parent) {
		// Main layout
		parent.setLayout(new GridLayout(1, false));

		// Create compact top section with all three sections in one row
		Composite topSection = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout(3, false);
		topLayout.marginWidth = 0;
		topLayout.marginHeight = 0;
		topLayout.horizontalSpacing = 5;
		topSection.setLayout(topLayout);
		topSection.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// DetailsComposite on the left (fixed width)
		cDesc = new DetailsComposite(topSection, this);

		// CasesComposite in the center (expands to fill available space)
		casesComposite = new CasesComposite(topSection, this);
		casesComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// TimerComposite on the right (fixed width)
		timerComposite = new TimerComposite(topSection, this);

		// Create SashForm for resizable sections
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create text area section in the sash form
		etf = new EnhancedTextField(sashForm, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		etf.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		hXrefs = new Hashtable<String, IKonsExtension>();

		// Enable cross-references
		@SuppressWarnings("unchecked")
		List<IKonsExtension> xrefs = Extensions
				.getClasses(Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION), "KonsExtension", false); //$NON-NLS-1$ //$NON-NLS-2$
		for (IKonsExtension x : xrefs) {
			String provider = x.connect(etf);
			hXrefs.put(provider, x);
		}
		etf.setXrefHandlers(hXrefs);

		// Enable Makros
		@SuppressWarnings("unchecked")
		List<IKonsMakro> makros = Extensions
				.getClasses(Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION), "KonsMakro", false); //$NON-NLS-1$ //$NON-NLS-2$
		etf.setExternalMakros(makros);

		// Create additions composite in the sash form with memento for state
		// persistence
		additionsComposite = new AdditionsComposite(sashForm, this, memento);

		// Set initial selection provider (billing positions is shown by default)
		getSite().setSelectionProvider(additionsComposite.getBillingPositionsComposite().getBillingPositionsViewer());

		// Restore saved weights from memento if available
		int[] weights = DEFAULT_WEIGHTS;
		if (memento != null) {
			String savedWeights = memento.getString("additions_height");
			if (savedWeights != null) {
				String[] parts = savedWeights.split(StringConstants.COMMA);
				if (parts.length == 2) {
					try {
						weights = new int[] { Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()) };
					} catch (NumberFormatException e) {
						// Use default weights if parsing fails
						weights = DEFAULT_WEIGHTS;
					}
				}
			}
		}
		sashForm.setWeights(weights);
		// Create toolbar actions
		createActions();
		contributeToActionBars();

		// Test the TardocManager
		System.out.println("TardocKonsView: Initializing TardocManager...");

		// Get BundleContext from this View's bundle
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		System.out.println("TardocKonsView: BundleContext obtained: " + (bundleContext != null));

		TardocManager manager = TardocManager.getInstance();

		if (manager.isServiceAvailable(bundleContext)) {
			System.out.println("TardocKonsView: Service is available, querying...");
			List<ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung> results = manager.getLeistungen("Konsultation",
					bundleContext);
			System.out.println("TardocManager returned " + results.size() + " results for 'Konsultation'");
			if (results.size() > 0) {
				System.out.println("First result: " + results.get(0).getCode() + " - " + results.get(0).getText());
			}
		} else {
			System.err.println("TardocKonsView: Service is NOT available");
		}
		IViewSite viewSite = getViewSite();

		etf.connectGlobalActions(viewSite);
		// adaptMenus();
		// initialize with currently selected encounter
		created = true;
		ContextServiceHolder.get().getTyped(IEncounter.class).ifPresent(e -> selectedEncounter(e));

		getSite().getPage().addPartListener(udpateOnVisible);

	}

	/**
	 * Creates the toolbar actions
	 */
	private void createActions() {
		toggleAdditionsAction = new Action("§", Action.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				toggleAdditionsVisibility();
			}
		};
		toggleAdditionsAction.setToolTipText(Messages.TardocKonsView_ToggleAdditions_Tooltip);
		toggleAdditionsAction.setImageDescriptor(Images.IMG_VIEW_PATIENT_DETAIL.getImageDescriptor());

		toggleViewAction = new Action("Dx", Action.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				toggleView();
			}
		};
		toggleViewAction.setToolTipText(Messages.TardocKonsView_SwitchToDiagnoses_Tooltip);
		toggleViewAction.setImageDescriptor(Images.IMG_BILL.getImageDescriptor());
	}

	/**
	 * Contributes actions to the view's toolbar
	 */
	private void contributeToActionBars() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(toggleAdditionsAction);
		toolBarManager.add(toggleViewAction);
	}

	/**
	 * Toggles the visibility of the additions section
	 */
	private void toggleAdditionsVisibility() {
		additionsVisible = !additionsVisible;

		if (additionsVisible) {
			// Show the additions section
			// Restore saved weights from memento if available
			int[] weights = DEFAULT_WEIGHTS;
			if (memento != null) {
				String savedWeights = memento.getString("additions_height");
				if (savedWeights != null) {
					String[] parts = savedWeights.split(StringConstants.COMMA);
					if (parts.length == 2) {
						try {
							weights = new int[] { Integer.parseInt(parts[0].trim()),
									Integer.parseInt(parts[1].trim()) };
						} catch (NumberFormatException e) {
							// Use default weights if parsing fails
							weights = DEFAULT_WEIGHTS;
						}
					}
				}
			}
			sashForm.setWeights(weights);
			sashForm.setMaximizedControl(null); // Restore normal layout
		} else {
			// Hide the additions section - maximize the text area section
			// Get the first child (text area section) and maximize it
			if (sashForm.getChildren().length > 0) {
				sashForm.setMaximizedControl(sashForm.getChildren()[0]);
			}
		}

		// Force layout update
		sashForm.layout(true, true);
	}

	/**
	 * Toggles between billing positions and diagnoses view in the additions
	 * composite
	 */
	private void toggleView() {
		if (additionsComposite != null && !additionsComposite.isDisposed()) {
			additionsComposite.toggleView();

			// Update the selection provider based on which view is showing
			if (additionsComposite.isShowingBillingPositions()) {
				getSite().setSelectionProvider(
						additionsComposite.getBillingPositionsComposite().getBillingPositionsViewer());
				toggleViewAction.setToolTipText(Messages.TardocKonsView_SwitchToDiagnoses_Tooltip);
			} else {
				getSite().setSelectionProvider(additionsComposite.getDiagnosesComposite().getDiagnosesViewer());
				toggleViewAction.setToolTipText(Messages.TardocKonsView_SwitchToBillingPositions_Tooltip);
			}
		}
	}

	@Override
	public void setFocus() {
		if (etf != null && !etf.isDisposed()) {
			etf.setFocus();
		}
	}

	@Override
	public void dispose() {
		created = false;
		this.encounterTimer.dispose();
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
	}

	@Override
	public void saveState(IMemento memento) {
		int[] w = sashForm.getWeights();
		memento.putString("additions_height", Integer.toString(w[0]) + StringConstants.COMMA + Integer.toString(w[1]));

		// Save additions composite state (including diagnoses code system selection)
		if (additionsComposite != null && !additionsComposite.isDisposed()) {
			additionsComposite.saveState(memento);
		}

		super.saveState(memento);
	}

}

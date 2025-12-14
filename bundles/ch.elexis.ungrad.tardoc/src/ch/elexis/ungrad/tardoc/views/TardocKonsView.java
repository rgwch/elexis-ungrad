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
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.ungrad.tardoc.services.BillingsManager;
import ch.elexis.ungrad.tardoc.services.TardocManager;
import ch.elexis.ungrad.tardoc.services.TardocManagerHolder;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;
import jakarta.inject.Inject;

/**
 * Tardoc consultation view with timer, text area, and billing positions list.
 * Features: - Timer with start/pause functionality - Free text entry area -
 * Billing positions list viewer
 */
public class TardocKonsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ch.elexis.ungrad.tardoc.views.TardocKonsView";

	Hashtable<String, IKonsExtension> hXrefs;
	EnhancedTextField text;
	private TimerComposite timerComposite;
	CasesComposite casesComposite;
	BillingsManager billingsManager = new BillingsManager(this);

	protected IEncounter actEncounter;
	IPatient actPat;
	private boolean created = false;

	private DetailsComposite cDesc;

	// Text area

	// Billing positions
	private BillingPositionsComposite billingPositionsComposite;

	private SashForm sashForm;
	private boolean billingPositionsVisible = true;
	private Action toggleBillingPositionsAction;

	private IPartListener2 udpateOnVisible = new IPartListener2() {
		@Override
		public void partActivated(org.eclipse.ui.IWorkbenchPartReference partRef) {
			if (actEncounter != null && !text.isDirty()) {
				setKonsText(actEncounter, actEncounter.getVersionedEntry().getHeadVersion());
			}
		}

		@Override
		public void partDeactivated(org.eclipse.ui.IWorkbenchPartReference partRef) {
			// save entry on deactivation if text was edited
			if (actEncounter != null && (text.isDirty())) {
				EncounterServiceHolder.get().updateVersionedEntry(actEncounter, text.getContentsAsXML(),
						getVersionRemark());
				text.setDirty(false);
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
		if (created) {
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

	private synchronized void setKons(final IEncounter encounter) {
		LoggerFactory.getLogger(getClass()).info("[KONS] " + (encounter != null ? encounter.getId() : "null"));

		if (actEncounter != null && text.isDirty()) {
			EncounterServiceHolder.get().updateVersionedEntry(actEncounter, text.getContentsAsXML(),
					getVersionRemark());
			text.setDirty(false);
		}
		cDesc.setEncounter(encounter);
		if (encounter != null) {
			ICoverage coverage = encounter.getCoverage();
			// TODO remove, pat should be already set via context
			setPatient(coverage.getPatient());
			setKonsText(encounter, encounter.getVersionedEntry().getHeadVersion());

			casesComposite.setEncounter(coverage);

			if (encounter.getDate().isEqual(LocalDate.now())) {
				text.setTextBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			} else {
				text.setTextBackground(UiDesk.getColor(UiDesk.COL_LIGHTBLUE)); // $NON-NLS-1$
			}
			billingsManager.setEncounter(encounter);
			billingPositionsComposite.setKons(encounter);
		} else {
			// diagnosesDisplay.clear();
			// billedDisplay.clear();
			text.setText(StringUtils.EMPTY);
			text.setEnabled(false);
			// billedDisplay.setEnabled(false);
			// diagnosesDisplay.setEnabled(false);
		}
		actEncounter = encounter;
		cDesc.layout();

	}

	void setKonsText(final IEncounter encounter, final int version) {
		String ntext = StringUtils.EMPTY;
		if ((version >= 0) && (version <= encounter.getVersionedEntry().getHeadVersion())) {
			VersionedResource vr = encounter.getVersionedEntry();
			ResourceItem entry = vr.getVersion(version);
			ntext = entry.data;
			StringBuilder sb = new StringBuilder();
			sb.append("rev. ").append(version).append(Messages.KonsDetailView_of) //$NON-NLS-1$
					.append( // $NON-NLS-2$
							new TimeTool(entry.timestamp).toString(TimeTool.FULL_GER))
					.append(" (").append(entry.remark).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		text.setText(ntext);
		text.setKons(encounter);
		// displayedVersion = version;
		// versionBackAction.setEnabled(version != 0);
		// versionFwdAction.setEnabled(version !=
		// encounter.getVersionedEntry().getHeadVersion());
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
		createTextAreaSection(sashForm);

		// Create billing positions section in the sash form
		createBillingPositionsSection(sashForm);

		// Set initial weights (70% text area, 30% billing positions)
		sashForm.setWeights(new int[] { 70, 30 });

		// Initialize timer display
		timerComposite.updateTimerDisplay();

		// Create toolbar actions
		createActions();
		contributeToActionBars();

		// Test the TardocManager
		System.out.println("TardocKonsView: Initializing TardocManager...");

		// Get BundleContext from this View's bundle
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		System.out.println("TardocKonsView: BundleContext obtained: " + (bundleContext != null));

		TardocManager manager = TardocManagerHolder.get();
		if (manager == null) {
			System.out.println("TardocKonsView: Holder returned null, creating instance directly");
			// Fallback: create instance directly (will use OSGi service lookup internally)
			manager = new TardocManager();
		} else {
			System.out.println("TardocKonsView: Got manager from holder");
		}

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

		text.connectGlobalActions(viewSite);
		// adaptMenus();
		// initialize with currently selected encounter
		created = true;
		ContextServiceHolder.get().getTyped(IEncounter.class).ifPresent(e -> selectedEncounter(e));

		getSite().getPage().addPartListener(udpateOnVisible);

	}

	
	/**
	 * Creates the text area section for free text entry
	 */
	private void createTextAreaSection(Composite parent) {
		Group textGroup = new Group(parent, SWT.NONE);
		textGroup.setText("Notes");
		textGroup.setLayout(new GridLayout(1, false));
		textGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		text = new EnhancedTextField(textGroup, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	}

	/**
	 * Creates the billing positions section with a composite viewer
	 */
	private void createBillingPositionsSection(Composite parent) {
		billingPositionsComposite = new BillingPositionsComposite(parent, SWT.NONE, this);

		// Inject E4 context to enable event handling
		ch.elexis.core.ui.e4.util.CoreUiUtil.injectServices(billingPositionsComposite);

		// Set selection provider for the site
		getSite().setSelectionProvider(billingPositionsComposite.getBillingPositionsViewer());
	}

	/**
	 * Creates the toolbar actions
	 */
	private void createActions() {
		toggleBillingPositionsAction = new Action("Toggle Billing Positions", Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				toggleBillingPositionsVisibility();
			}
		};
		toggleBillingPositionsAction.setToolTipText("Show/Hide Billing Positions");
		toggleBillingPositionsAction.setChecked(billingPositionsVisible);
		// You can add an icon here if available:
		// toggleBillingPositionsAction.setImageDescriptor(...)
	}

	/**
	 * Contributes actions to the view's toolbar
	 */
	private void contributeToActionBars() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(toggleBillingPositionsAction);
	}

	/**
	 * Toggles the visibility of the billing positions section
	 */
	private void toggleBillingPositionsVisibility() {
		billingPositionsVisible = !billingPositionsVisible;

		if (billingPositionsVisible) {
			// Show the billing positions
			if (billingPositionsComposite != null && !billingPositionsComposite.isDisposed()) {
				billingPositionsComposite.setVisible(true);
				sashForm.setWeights(new int[] { 70, 30 });
				sashForm.setMaximizedControl(null); // Restore normal layout
			}
		} else {
			// Hide the billing positions - maximize the text area section
			if (billingPositionsComposite != null && !billingPositionsComposite.isDisposed()) {
				// Get the first child (text area section) and maximize it
				if (sashForm.getChildren().length > 0) {
					sashForm.setMaximizedControl(sashForm.getChildren()[0]);
				}
			}
		}

		// Force layout update
		sashForm.layout(true, true);
		toggleBillingPositionsAction.setChecked(billingPositionsVisible);
	}

	@Override
	public void setFocus() {
		if (text != null && !text.isDisposed()) {
			text.setFocus();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}

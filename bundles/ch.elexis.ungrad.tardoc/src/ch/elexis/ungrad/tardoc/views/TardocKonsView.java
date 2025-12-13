package ch.elexis.ungrad.tardoc.views;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ac.EvACEs;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.util.CoverageComparator;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.provider.CoverageColorLabelProvider;
import ch.elexis.data.Mandant;
import ch.elexis.ungrad.tardoc.services.BillingsManager;
import ch.elexis.ungrad.tardoc.services.TardocManager;
import ch.elexis.ungrad.tardoc.services.TardocManagerHolder;
import ch.rgw.tools.Result;
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
	private static final String NO_CONS_SELECTED = Messages.KonsDetailView_NoConsSelected; // $NON-NLS-1$

	Hashtable<String, IKonsExtension> hXrefs;
	EnhancedTextField text;
	private Hyperlink hlMandant, hlDate;
	TableComboViewer tableComboViewerFall;
	private ComboFallSelectionListener comboFallSelectionListener;
	BillingsManager billingsManager = new BillingsManager(this);
	private FormToolkit tk = UiDesk.getToolkit();

	protected IEncounter actEncounter;
	IPatient actPat;
	private boolean created = false;

	private Composite cDesc;

	// Timer components
	private Label timerLabel;
	private Button startPauseButton;
	private Button resetButton;
	private long startTime;
	private long pausedTime;
	private boolean isRunning = false;
	private boolean isPaused = false;
	private Runnable timerRunnable;

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
		IPatient pat = ContextServiceHolder.get().getRootContext().getTyped(IPatient.class).orElse(null);
		if (pat != null && tableComboViewerFall != null) {
			List<ICoverage> coverages = pat.getCoverages();
			Collections.sort(coverages, new CoverageComparator());
			tableComboViewerFall.setInput(coverages);
			if (actEncounter != null) {
				comboFallSelectionListener.ignoreSelectionEventOnce();
				tableComboViewerFall.setSelection(new StructuredSelection(actEncounter.getCoverage()));
			}
			// needed for initial background colours
			tableComboViewerFall.getTableCombo().setTableVisible(true);
			tableComboViewerFall.getTableCombo().setTableVisible(false);
			tableComboViewerFall.refresh();
		}
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

		if (encounter != null) {
			ICoverage coverage = encounter.getCoverage();
			// TODO remove, pat should be already set via context
			setPatient(coverage.getPatient());
			setKonsText(encounter, encounter.getVersionedEntry().getHeadVersion());

			comboFallSelectionListener.ignoreSelectionEventOnce();
			tableComboViewerFall.setSelection(new StructuredSelection(coverage));
			tableComboViewerFall.getTableCombo().setEnabled(coverage.isOpen());
			IMandator mandator = encounter.getMandator();
			String encounterDate = TimeUtil.formatSafe(encounter.getDate());
			hlDate.setText(encounterDate + " (" //$NON-NLS-1$
					+ new TimeTool(encounter.getDate()).getDurationToNowString() + ")"); //$NON-NLS-1$
			StringBuilder sb = new StringBuilder();
			if (mandator == null) {
				sb.append(Messages.KonsDetailView_NotYours); // $NON-NLS-1$
				hlMandant.setBackground(hlMandant.getParent().getBackground());
			} else {
				IContact biller = mandator.getBiller();
				if (biller.getId().equals(mandator.getId())) {
					sb.append("(").append(mandator.getLabel()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					sb.append("(").append(mandator.getLabel()).append("/").append( //$NON-NLS-1$ //$NON-NLS-2$
							biller.getLabel()).append(")"); //$NON-NLS-1$
				}
				hlMandant.setBackground(UiMandant.getColorForMandator(Mandant.load(mandator.getId())));
			}
			hlMandant.setText(sb.toString());

			boolean hlMandantEnabled = BillingServiceHolder.get().isEditable(encounter).isOK()
					&& AccessControlServiceHolder.get().evaluate(EvACEs.KONS_REASSIGN);
			hlMandant.setEnabled(hlMandantEnabled);
			// diagnosesDisplay.setEncounter(encounter);
			// billedDisplay.setEncounter(encounter);
			// billedDisplay.setEnabled(true);
			// diagnosesDisplay.setEnabled(true);
			if (BillingServiceHolder.get().isEditable(encounter).isOK()) {
				text.setEnabled(true);
				text.setToolTipText(StringUtils.EMPTY);
				hlDate.setForeground(UiDesk.getColor(UiDesk.COL_BLACK));
				// hlDate.setBackground(defaultBackground);
			} else {
				text.setToolTipText("Konsultation geschlossen oder nicht von Ihnen");
				hlDate.setForeground(UiDesk.getColor(UiDesk.COL_WHITE));
				hlDate.setBackground(UiDesk.getColor(UiDesk.COL_GREY20));
			}
			if (encounter.getDate().isEqual(LocalDate.now())) {
				text.setTextBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			} else {
				text.setTextBackground(UiDesk.getColorFromRGB("FAFAFA")); //$NON-NLS-1$
			}
			billingPositionsComposite.setKons(encounter);
		} else {
			hlDate.setText("-"); //$NON-NLS-1$
			hlMandant.setText("--"); //$NON-NLS-1$
			hlMandant.setEnabled(false);
			hlMandant.setBackground(hlMandant.getParent().getBackground());
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

		createDetailsSection(parent);

		createFallSection(parent);

		// Create timer section
		createTimerSection(parent);

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
		updateTimerDisplay();

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

	private void createDetailsSection(final Composite parent) {
		cDesc = new Composite(parent, SWT.NONE);
		cDesc.setLayout(new RowLayout(SWT.HORIZONTAL));
		cDesc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		// emFont = UiDesk.getFont("Helvetica", 11, SWT.BOLD); //$NON-NLS-1$
		// defaultBackground = p.getBackground();
		hlDate = tk.createHyperlink(cDesc, NO_CONS_SELECTED, SWT.NONE);
		// hlDate.setFont(emFont);
		hlDate.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				GlobalActions.redateAction.reflectRight();
				if (GlobalActions.redateAction.isEnabled()) {
					GlobalActions.redateAction.doRun();
				}
			}
		});

		hlMandant = tk.createHyperlink(cDesc, "--", SWT.NONE); //$NON-NLS-1$
		hlMandant.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				// CommonViewer of KontaktSelektor will set Mandant selection of
				// ElexisEventDispatcher
				// we want do reset to current mandant afterwards
				Mandant currentMandant = ElexisEventDispatcher.getSelectedMandator();
				KontaktSelektor ksl = new KontaktSelektor(getSite().getShell(), Mandant.class,
						Messages.Core_Select_Mandator, // $NON-NLS-1$
						Messages.KonsDetailView_SelectMandatorBody,
						new String[] { Mandant.FLD_SHORT_LABEL, Mandant.FLD_NAME1, Mandant.FLD_NAME2 }); // $NON-NLS-1$
				ksl.disableContextSelection();
				if (ksl.open() == Dialog.OK) {
					IMandator mandator = CoreModelServiceHolder.get()
							.load(((Mandant) ksl.getSelection()).getId(), IMandator.class).orElse(null);
					if (mandator != null) {
						Result<IEncounter> result = EncounterServiceHolder.get().transferToMandator(actEncounter,
								mandator, false);
						if (!result.isOK()) {
							MessageDialog.openError(getSite().getShell(), Messages.Core_Error,
									result.getCombinedMessages());
						}
					}
				}
				ElexisEventDispatcher.fireSelectionEvent(currentMandant);
			}

		});
		hlMandant.setBackground(parent.getBackground());

	}

	private void createFallSection(final Composite parent) {
		tableComboViewerFall = new TableComboViewer(parent, SWT.SINGLE | SWT.BORDER);
		tableComboViewerFall.setContentProvider(ArrayContentProvider.getInstance());
		tableComboViewerFall.setLabelProvider(new CoverageColorLabelProvider());

		comboFallSelectionListener = new ComboFallSelectionListener(this);
		tableComboViewerFall.addSelectionChangedListener(comboFallSelectionListener);
		GridData gdFall = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		tableComboViewerFall.getTableCombo().setLayoutData(gdFall);
		tableComboViewerFall.getTableCombo().setTableVisible(true);
		tableComboViewerFall.getTableCombo().setTableVisible(false);

	}

	/**
	 * Creates the timer section with start/pause and reset buttons
	 */
	private void createTimerSection(Composite parent) {
		Group timerGroup = new Group(parent, SWT.NONE);
		timerGroup.setText("Timer");
		timerGroup.setLayout(new GridLayout(3, false));
		timerGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Timer display
		timerLabel = new Label(timerGroup, SWT.NONE);
		timerLabel.setText("00:00");
		timerLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Start/Pause button
		startPauseButton = new Button(timerGroup, SWT.PUSH);
		startPauseButton.setText("Start");
		startPauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleTimer();
			}
		});

		// Reset button
		resetButton = new Button(timerGroup, SWT.PUSH);
		resetButton.setText("Reset");
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetTimer();
			}
		});
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

	/**
	 * Toggles the timer between start/pause states
	 */
	private void toggleTimer() {
		if (!isRunning) {
			startTimer();
		} else {
			pauseTimer();
		}
	}

	/**
	 * Starts the timer
	 */
	private void startTimer() {
		if (isPaused) {
			// Resume from paused state
			startTime = System.currentTimeMillis() - pausedTime;
			isPaused = false;
		} else {
			// Fresh start
			startTime = System.currentTimeMillis();
			pausedTime = 0;
		}

		isRunning = true;
		startPauseButton.setText("Pause");

		// Create and schedule timer runnable
		timerRunnable = new Runnable() {
			@Override
			public void run() {
				if (isRunning && !timerLabel.isDisposed()) {
					updateTimerDisplay();
					Display.getCurrent().timerExec(1000, this);
				}
			}
		};
		Display.getCurrent().timerExec(1000, timerRunnable);
	}

	/**
	 * Pauses the timer
	 */
	private void pauseTimer() {
		isRunning = false;
		isPaused = true;
		pausedTime = System.currentTimeMillis() - startTime;
		startPauseButton.setText("Start");
	}

	/**
	 * Resets the timer to 00:00
	 */
	private void resetTimer() {
		isRunning = false;
		isPaused = false;
		startTime = 0;
		pausedTime = 0;
		startPauseButton.setText("Start");
		updateTimerDisplay();
	}

	/**
	 * Updates the timer display label
	 */
	private void updateTimerDisplay() {
		if (timerLabel == null || timerLabel.isDisposed()) {
			return;
		}

		long elapsedTime = 0;
		if (isRunning) {
			elapsedTime = System.currentTimeMillis() - startTime;
		} else if (isPaused) {
			elapsedTime = pausedTime;
		}

		long minutes = elapsedTime / (60 * 1000);
		long seconds = (elapsedTime % (60 * 1000)) / 1000;

		String timeString = String.format("%02d:%02d", minutes, seconds);
		timerLabel.setText(timeString);
	}

	@Override
	public void setFocus() {
		if (text != null && !text.isDisposed()) {
			text.setFocus();
		}
	}

	@Override
	public void dispose() {
		// Stop timer when view is disposed
		if (isRunning) {
			isRunning = false;
		}
		super.dispose();
	}

}

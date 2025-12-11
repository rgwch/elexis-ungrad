package ch.elexis.ungrad.tardoc.views;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.ui.services.EncounterServiceHolder;

import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.util.CoverageComparator;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.ungrad.tardoc.services.TardocManager;
import ch.elexis.ungrad.tardoc.services.TardocManagerHolder;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Tardoc consultation view with timer, text area, and billing positions list.
 * Features:
 * - Timer with start/pause functionality
 * - Free text entry area
 * - Billing positions list viewer
 */
public class TardocKonsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ch.elexis.ungrad.tardoc.views.TardocKonsView";
	Hashtable<String, IKonsExtension> hXrefs;
	EnhancedTextField text;
	private Hyperlink hlMandant, hlDate;
	TableComboViewer tableComboViewerFall;
	private ComboFallSelectionListener comboFallSelectionListener;

	protected IEncounter actEncounter;
	FormToolkit tk;
	Form form;
	IPatient actPat;
	private boolean created = false;
	

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
	
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this) {

		@Override
		public void partActivated(org.eclipse.ui.IWorkbenchPartReference partRef) {
			if (isMatchingPart(partRef)) {
				if (actEncounter != null && !text.isDirty()) {
					setKonsText(actEncounter, actEncounter.getVersionedEntry().getHeadVersion());
				}
			}
		};

		@Override
		public void partDeactivated(org.eclipse.ui.IWorkbenchPartReference partRef) {
			if (isMatchingPart(partRef)) {
				// save entry on deactivation if text was edited
				if (actEncounter != null && (text.isDirty())) {
					EncounterServiceHolder.get().updateVersionedEntry(actEncounter, text.getContentsAsXML(),
							getVersionRemark());
					text.setDirty(false);
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, actEncounter);
				}
			}
		};

		@Override
		public void partVisible(org.eclipse.ui.IWorkbenchPartReference partRef) {
			// do not call super and refresh, should be handled by partActivated
			if (isMatchingPart(partRef)) {
				// adaptMenus();
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
				//adaptMenus();
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
	
	/** Aktuellen patient setzen */
	private synchronized void setPatient(IPatient pat) {
		LoggerFactory.getLogger(getClass()).info("[KONS PAT] " + (pat != null ? pat.getId() : "null"));
		if (pat != null && actPat != null) {
			if (pat.getId().equals(actPat.getId())) {
				if (!form.getText().equals(Messages.KonsDetailView_NoConsSelected)) {
					return;
				}
			}
		}

		actPat = pat;
		if (pat != null) {
			form.setText(pat.getLabel() + StringTool.space + "(" + pat.getAgeInYears() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			// stickerComposite.setPatient(pat);
			updateFallCombo();
		}
		form.layout();
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
		// versionFwdAction.setEnabled(version != encounter.getVersionedEntry().getHeadVersion());
	}


	@Override
	public void createPartControl(Composite parent) {
		// Main layout
		parent.setLayout(new GridLayout(1, false));

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
			List<ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung> results = manager.getLeistungen("Konsultation", bundleContext);
			System.out.println("TardocManager returned " + results.size() + " results for 'Konsultation'");
			if (results.size() > 0) {
				System.out.println("First result: " + results.get(0).getCode() + " - " + results.get(0).getText());
			}
		} else {
			System.err.println("TardocKonsView: Service is NOT available");
		}
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
		billingPositionsComposite = new BillingPositionsComposite(parent, SWT.NONE);
		
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

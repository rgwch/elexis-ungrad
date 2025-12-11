package ch.elexis.ungrad.tardoc.views;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
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

import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.ungrad.tardoc.services.TardocManager;
import ch.elexis.ungrad.tardoc.services.TardocManagerHolder;
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
	// TableComboViewer tableComboViewerFall;
	private IEncounter actEncounter;
	FormToolkit tk;
	Form form;
	IPatient actPat;

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
	private Text textArea;

	// Billing positions
	private BillingPositionsComposite billingPositionsComposite;
	private SashForm sashForm;
	private boolean billingPositionsVisible = true;
	private Action toggleBillingPositionsAction;

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

		textArea = new Text(textGroup, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		textArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		textArea.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
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
		if (textArea != null && !textArea.isDisposed()) {
			textArea.setFocus();
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

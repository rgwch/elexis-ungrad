package ch.elexis.ungrad.tardoc.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import jakarta.inject.Inject;

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

	// Billing positions list
	private TableViewer billingPositionsViewer;

	/**
	 * Label provider for billing positions
	 */
	class BillingPositionLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		
		@Override
		public Image getColumnImage(Object obj, int index) {
			return null;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		// Main layout
		parent.setLayout(new GridLayout(1, false));

		// Create timer section
		createTimerSection(parent);

		// Create text area section
		createTextAreaSection(parent);

		// Create billing positions section
		createBillingPositionsSection(parent);

		// Initialize timer display
		updateTimerDisplay();
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
	 * Creates the billing positions section with a list viewer
	 */
	private void createBillingPositionsSection(Composite parent) {
		Group billingGroup = new Group(parent, SWT.NONE);
		billingGroup.setText("Billing Positions");
		billingGroup.setLayout(new GridLayout(1, false));
		billingGroup.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

		billingPositionsViewer = new TableViewer(billingGroup, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		billingPositionsViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		billingPositionsViewer.getTable().setHeaderVisible(true);
		billingPositionsViewer.getTable().setLinesVisible(true);

		// Set content and label provider
		billingPositionsViewer.setContentProvider(ArrayContentProvider.getInstance());
		billingPositionsViewer.setLabelProvider(new BillingPositionLabelProvider());

		// Initialize with sample data (to be replaced with database query later)
		List<String> samplePositions = new ArrayList<>();
		samplePositions.add("00.0010 - Consultation, first 5 min");
		samplePositions.add("00.0020 - Consultation, each additional 5 min");
		samplePositions.add("00.0030 - Consultation by phone");
		samplePositions.add("00.0040 - Emergency consultation");
		samplePositions.add("00.0050 - Visit at patient's home");
		
		billingPositionsViewer.setInput(samplePositions);

		// Set selection provider for the site
		getSite().setSelectionProvider(billingPositionsViewer);
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

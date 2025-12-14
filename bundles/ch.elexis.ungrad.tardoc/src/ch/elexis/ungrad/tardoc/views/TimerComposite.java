package ch.elexis.ungrad.tardoc.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class TimerComposite extends Composite {
	TardocKonsView tkv;
	// Timer components
	private Label timerLabel;
	private Button startPauseButton;
	private Button resetButton;
	private long startTime;
	private long pausedTime;
	private boolean isRunning = false;
	private boolean isPaused = false;
	private Runnable timerRunnable;

	public TimerComposite(Composite parent, TardocKonsView view) {
		super(parent, SWT.NONE);
		this.tkv = view;
		GridLayout timerLayout = new GridLayout(3, false);
		timerLayout.marginWidth = 0;
		timerLayout.marginHeight = 0;
		timerLayout.horizontalSpacing = 3;
		setLayout(timerLayout);
		setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		// Timer display
		timerLabel = new Label(this, SWT.NONE);
		timerLabel.setText("00:00");
		GridData gdTimer = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gdTimer.widthHint = 50;
		timerLabel.setLayoutData(gdTimer);

		// Start/Pause button
		startPauseButton = new Button(this, SWT.PUSH);
		startPauseButton.setText("Start");
		startPauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleTimer();
			}
		});

		// Reset button
		resetButton = new Button(this, SWT.PUSH);
		resetButton.setText("Reset");
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetTimer();
			}
		});

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
	 * Starts the timer. Auto-Adds billing positions based on mandator's Dignity,
	 */
	private void startTimer() {
		if (isPaused) {
			// Resume from paused state
			startTime = System.currentTimeMillis() - pausedTime;
			isPaused = false;
		} else {
			// Fresh start - check conditions for auto-billing CA00.0010
			startTime = System.currentTimeMillis();
			pausedTime = 0;

			// Auto-bill CA00.0010 if conditions are met
			this.tkv.billingsManager.autostart();

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
	void updateTimerDisplay() {
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
	public void dispose() {
		// Stop timer when view is disposed
		if (isRunning) {
			isRunning = false;
		}
		super.dispose();
	}

}

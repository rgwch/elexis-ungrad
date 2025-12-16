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
 *********************************************************************************/

package ch.elexis.ungrad.tardoc.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Composite for a start/pause/reset timer with auto-billing capabilities.
 */
public class TimerComposite extends Composite {
	TardocKonsView tkv;
	// Timer components
	private Label timerLabel;
	private Button startPauseButton;
	private Button resetButton;

	// Icons
	private Image playIcon;
	private Image pauseIcon;
	private Image stopIcon;

	public TimerComposite(Composite parent, TardocKonsView view) {
		super(parent, SWT.NONE);
		this.tkv = view;

		// Load icons
		loadIcons();

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
		startPauseButton.setImage(playIcon);
		startPauseButton.setToolTipText("Start timer");
		GridData gdStartPause = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gdStartPause.widthHint = 24;
		gdStartPause.heightHint = 24;
		startPauseButton.setLayoutData(gdStartPause);
		startPauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleTimer();
			}
		});

		// Reset button
		resetButton = new Button(this, SWT.PUSH);
		resetButton.setImage(stopIcon);
		resetButton.setToolTipText("Reset timer");
		GridData gdReset = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gdReset.widthHint = 24;
		gdReset.heightHint = 24;
		resetButton.setLayoutData(gdReset);
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetTimer();
			}
		});
		this.tkv.encounterTimer.addTimerListener(newTime -> {
			getDisplay().asyncExec(() -> updateDisplay(newTime));
		});
	}

	/**
	 * Loads the icon images from the icons folder
	 */
	private void loadIcons() {
		Bundle bundle = FrameworkUtil.getBundle(getClass());

		ImageDescriptor playDesc = ImageDescriptor.createFromURL(bundle.getEntry("icons/media-play-2x.png"));
		playIcon = playDesc.createImage();

		ImageDescriptor pauseDesc = ImageDescriptor.createFromURL(bundle.getEntry("icons/media-pause-2x.png"));
		pauseIcon = pauseDesc.createImage();

		ImageDescriptor stopDesc = ImageDescriptor.createFromURL(bundle.getEntry("icons/media-stop-2x.png"));
		stopIcon = stopDesc.createImage();
	}

	/**
	 * Toggles the timer between start/pause states
	 */
	private void toggleTimer() {
		if (!this.tkv.encounterTimer.isRunning()) {
			this.tkv.encounterTimer.resume();
			startPauseButton.setImage(pauseIcon);
			startPauseButton.setToolTipText("Pause timer");
		} else {
			this.tkv.encounterTimer.pause();
			startPauseButton.setImage(playIcon);
			startPauseButton.setToolTipText("Start timer");
		}
	}

	/**
	 * Resets the timer to 00:00
	 */
	private void resetTimer() {
		startPauseButton.setImage(playIcon);
		startPauseButton.setToolTipText("Start timer");
		this.tkv.encounterTimer.stop();
		updateDisplay(0L);
	}

	public void updateDisplay(long elapsedSeconds) {
		long minutes = elapsedSeconds / 60;
		long seconds = elapsedSeconds % 60;
		String timeString = String.format("%02d:%02d", minutes, seconds);
		if (timerLabel != null && !timerLabel.isDisposed()) {
			timerLabel.setText(timeString);
		}
		if (this.tkv.encounterTimer.isRunning()) {
			this.tkv.billingsManager.autobill(minutes);
			startPauseButton.setImage(pauseIcon);
			startPauseButton.setToolTipText("Pause timer");
		} else {
			startPauseButton.setImage(playIcon);
			startPauseButton.setToolTipText("Start timer");
		}
	}

	@Override
	public void dispose() {
		// Stop timer when view is disposed
		this.tkv.encounterTimer.stop();
		// Dispose of icon images
		if (playIcon != null && !playIcon.isDisposed()) {
			playIcon.dispose();
		}
		if (pauseIcon != null && !pauseIcon.isDisposed()) {
			pauseIcon.dispose();
		}
		if (stopIcon != null && !stopIcon.isDisposed()) {
			stopIcon.dispose();
		}

		super.dispose();
	}

}

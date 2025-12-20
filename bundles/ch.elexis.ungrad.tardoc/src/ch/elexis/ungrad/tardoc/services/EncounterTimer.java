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

package ch.elexis.ungrad.tardoc.services;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer for encounters with start, pause, resume and stop capabilities. Used
 * for Auto-billing.
 */
public class EncounterTimer extends Timer {
	public interface TimerListener {
		void onTick(long elapsedSeconds);
	}

	private long startTime = 0L;
	private long pausedTime = 0L;
	private boolean isRunning = false;
	private LinkedList<TimerListener> listeners = new LinkedList<>();

	public EncounterTimer() {
		super("ElexisEncounterTimer", true);
		scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!isRunning) {
					return;
				}
				publish();
			}
		}, 0L, 1000L);
	}

	private void publish() {
		long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;

		if (startTime == 0L) {
			elapsedSeconds = 0L;
		}
		for (TimerListener listener : listeners) {
			listener.onTick(elapsedSeconds);
		}
	}

	public void start() {
		isRunning = true;
		this.startTime = System.currentTimeMillis();
	}

	public void pause() {
		this.isRunning = false;
		this.pausedTime = System.currentTimeMillis() - startTime;
		publish();
	}

	public void resume() {
		if (this.startTime == 0) {
			this.startTime = System.currentTimeMillis();
		} else {
			this.startTime = System.currentTimeMillis() - pausedTime;
			this.pausedTime = 0L;
		}
		this.isRunning = true;
		publish();
	}

	public void stop() {
		this.isRunning = false;
		this.startTime = 0L;
		publish();
	}

	public void dispose() {
		for (TimerListener listener : listeners) {
			this.removeTimerListener(listener);
		}
		this.cancel();
	}

	public boolean isRunning() {
		return this.isRunning;
	}

	public void addTimerListener(TimerListener listener) {
		this.listeners.add(listener);
	}

	public void removeTimerListener(TimerListener listener) {
		this.listeners.remove(listener);
	}
}

package ch.elexis.ungrad.tardoc.services;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import ch.elexis.ungrad.tardoc.views.TardocKonsView;

public class EncounterTimer extends Timer {
	public interface TimerListener {
		void onTick(long elapsedSeconds);
	}

	private TardocKonsView tkv;
	private long startTime=0L;
	private long pausedTime=0L;
	private boolean isRunning = false;
	private LinkedList<TimerListener> listeners = new LinkedList<>();

	public EncounterTimer(TardocKonsView view) {
		super("ElexisEncounterTimer", true);
		this.tkv = view;
		scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!isRunning) {
					return;
				}
				long elapsed = (System.currentTimeMillis() - startTime) / 1000;
				for (TimerListener listener : listeners) {
					listener.onTick(elapsed);
				}
			}
		}, 0L, 1000L);
	}

	public void start() {
		isRunning = true;
		this.startTime = System.currentTimeMillis();
	}

	public void pause() {
		this.isRunning = false;
		this.pausedTime = System.currentTimeMillis() - startTime;
	}

	public void resume() {
		if (this.startTime == 0) {
			this.startTime = System.currentTimeMillis();
		}else {
			this.startTime = System.currentTimeMillis() - pausedTime;
			this.pausedTime = 0L;
		}
		this.isRunning = true;
	}

	public void stop() {
		this.isRunning = false;
		this.startTime = 0;
	}

	public void dispose() {
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

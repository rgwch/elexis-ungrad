package ch.elexis.ungrad;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.eclipse.swt.widgets.Display;

public class ObservableFile {
	private boolean cancelled = false;
	private WatchService watcher;
	private File tempFile;

	public ObservableFile(File dir, IObserver observer) throws IOException {
		watcher = FileSystems.getDefault().newWatchService();
		tempFile = File.createTempFile("ungr", "tmp", dir);
		tempFile.deleteOnExit();
		Path watchdir = dir.toPath();
		WatchKey key = watchdir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
		asyncWatchDir(observer);
	}

	public void cancel() {
		cancelled = true;
	}

	void asyncWatchDir(IObserver observer) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				WatchKey watchKey = null;
				try {
					while ((watchKey = watcher.take()) != null) {
						watchKey.pollEvents().stream().forEach(event -> observer.signal(event.context()));
						watchKey.reset();

						// cancel the watch key
						if (cancelled) {
							watchKey.cancel();
							tempFile.delete();
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}
}

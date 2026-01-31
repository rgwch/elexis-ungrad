package ch.elexis.ungrad.whisper;


import org.eclipse.swt.widgets.Display;

public class WhisperService {

 private final WhisperRecorder recorder = new WhisperRecorder();
 private volatile boolean active;
 private ITextConsumer textConsumer;
 private Thread poller;

 public interface ITextConsumer {
     void onText(String text);
 }

 public void setTextConsumer(ITextConsumer consumer) {
     this.textConsumer = consumer;
 }

 public boolean start(String modelPath) {
     if (active) return true;
     if (!WhisperNative.init(modelPath)) {
         return false;
     }
     active = true;
     recorder.start();
     startPolling();
     return true;
 }

 public void stop() {
     if (!active) return;
     active = false;
     recorder.stop();
     WhisperNative.shutdown();
     if (poller != null) {
         poller.interrupt();
     }
 }

 private void startPolling() {
     poller = new Thread(() -> {
         while (active) {
             String text = WhisperNative.getAndClearText();
             if (text != null && !text.isEmpty() && textConsumer != null) {
                 String copy = text;
                 Display.getDefault().asyncExec(() -> textConsumer.onText(copy));
             }
             try {
                 Thread.sleep(500);
             } catch (InterruptedException e) {
                 break;
             }
         }
     }, "WhisperPoller");
     poller.setDaemon(true);
     poller.start();
 }
}

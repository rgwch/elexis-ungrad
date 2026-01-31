package ch.elexis.ungrad.whisper;

import javax.sound.sampled.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WhisperRecorder implements Runnable {

 private final AtomicBoolean running = new AtomicBoolean(false);
 private Thread thread;

 private final int sampleRate = 16000;      // Whisper-Standard
 private final int chunkMillis = 1000;      // 1 Sekunde pro Chunk

 public void start() {
     if (running.compareAndSet(false, true)) {
         thread = new Thread(this, "WhisperRecorder");
         thread.setDaemon(true);
         thread.start();
     }
 }

 public void stop() {
     running.set(false);
     if (thread != null) {
         try {
             thread.join();
         } catch (InterruptedException ignored) { }
     }
 }

 @Override
 public void run() {
     AudioFormat format = new AudioFormat(
             sampleRate,
             16,      // Bits
             1,       // Mono
             true,
             false    // little endian
     );

     DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
     try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
         line.open(format);
         line.start();

         int bytesPerSecond = (int) (format.getFrameSize() * sampleRate);
         int bufferSize = bytesPerSecond * chunkMillis / 1000;
         byte[] buffer = new byte[bufferSize];

         while (running.get()) {
             int read = line.read(buffer, 0, buffer.length);
             if (read <= 0) continue;

             // 16-bit PCM -> float [-1,1]
             int samples = read / 2;
             float[] pcm = new float[samples];
             for (int i = 0; i < samples; i++) {
                 int lo = buffer[2 * i] & 0xff;
                 int hi = buffer[2 * i + 1];
                 int val = (hi << 8) | lo;
                 pcm[i] = val / 32768.0f;
             }

             // Non-blocking wäre schöner, hier der Einfachheit halber direkt:
             WhisperNative.processAudio(pcm, sampleRate, false);
         }

         // Letzter Chunk markieren
         WhisperNative.processAudio(new float[0], sampleRate, true);

     } catch (LineUnavailableException e) {
         e.printStackTrace();
     }
 }
}

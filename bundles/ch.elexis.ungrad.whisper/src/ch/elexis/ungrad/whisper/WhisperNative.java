package ch.elexis.ungrad.whisper;

public class WhisperNative {

    static {
        // libwhisperjni.so muss in java.library.path liegen
        System.loadLibrary("whisperjni");
    	// WhisperJNI.loadLibrary();
    }

    public static native boolean init(String modelPath);
    public static native void shutdown();
    public static native boolean processAudio(float[] pcm, int sampleRate, boolean lastChunk);
    public static native String getAndClearText();
}

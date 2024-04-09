package client;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class RecordAudio implements Runnable {
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    
    private AudioFormat format;
    private ByteArrayOutputStream out;
    private boolean isRecording = false;
    private TargetDataLine targetLine;
    private Thread recordingThread;

    public RecordAudio() {
        this.format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        this.out = new ByteArrayOutputStream();
    }

    public void startRecording() {
        isRecording = true;
        recordingThread = new Thread(this);
        recordingThread.start();
    }

    public void stopRecording() {
        isRecording = false;
        if (recordingThread != null && recordingThread.isAlive()) {
            recordingThread.interrupt();
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public void run() {
        int bytesRead;
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            targetLine.start();

            byte[] buffer = new byte[targetLine.getBufferSize() / 5];
            while (isRecording) {
                bytesRead = targetLine.read(buffer, 0, buffer.length);
                out.write(buffer, 0, bytesRead);
            }

            targetLine.stop();
            targetLine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getAudioData() {
        return out.toByteArray();
    }
}
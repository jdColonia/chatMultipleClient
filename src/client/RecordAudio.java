package client;
import java.io.ByteArrayOutputStream;
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
    private volatile boolean isRecording = false;
    private TargetDataLine targetLine;

    public RecordAudio() {
        this.format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        this.out = new ByteArrayOutputStream();
    }

    public void startRecording() {
        isRecording = true;
        new Thread(this).start();
    }

    public void stopRecording() {
        isRecording = false;
    }

    @Override
    public void run() {
        int bytesRead;
        try {
            // Abrir línea de captura de audio
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            // Comenzar la captura de audio
            targetLine.start();
            // Grabar audio hasta que se dé la señal de parar
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
import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class AudioRecorderPlayer {

    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    public void recordAudio(ByteArrayOutputStream byteArrayOutputStream) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            targetLine.start();
            byte[] buffer = new byte[1024];
            int bytesRead;
    
            System.out.println("Grabando durante " + 5 + " segundos...");
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 5 * 1000) {
                bytesRead = targetLine.read(buffer, 0, buffer.length);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
    
            // Detener y cerrar la línea de captura...
            targetLine.stop();
            targetLine.close();
    
        } catch (Exception e) {
            System.err.println("Error al grabar audio: " + e.getMessage());
  
        }
    }

    
}

import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class PlayerRecording {
    private AudioFormat format;
    private SourceDataLine out;
    private AudioInputStream in;

    public PlayerRecording(AudioFormat format) {
        this.format = format;
    }

    public void initiateAudio(byte[] audioData) {
        try {
            in = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize());
            // Abrir línea de salida de audio
            out = AudioSystem.getSourceDataLine(format);
            out.open(format);
            // Comenzar la reproducción de audio
            out.start();
            playAudio();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void playAudio() {
        byte[] buffer = new byte[1024];
        int count;
        try {
            // Leer datos de audio de la entrada y escribirlos en la salida
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            out.drain();
            out.stop();
            out.close();
            in.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
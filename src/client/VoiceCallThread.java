package client;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VoiceCallThread extends Thread {
    private final Client client;
    private final AudioFormat audioFormat;
    private final TargetDataLine targetDataLine;
    private final SourceDataLine sourceDataLine;

    public VoiceCallThread(Client client) throws LineUnavailableException {
        this.client = client;
        this.audioFormat = getAudioFormat();
        this.targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        this.sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
    }

    @Override
    public void run() {
        try {
            targetDataLine.open(audioFormat);
            sourceDataLine.open(audioFormat);
            targetDataLine.start();
            sourceDataLine.start();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[10000];
            boolean isRecording = true;

            while (isRecording) {
                int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                if (cnt > 0) {
                    byteArrayOutputStream.write(tempBuffer, 0, cnt);
                    byte[] audioData = byteArrayOutputStream.toByteArray();
                    client.sendVoiceData(audioData); // Método para enviar datos de voz al servidor
                }
            }

            byteArrayOutputStream.close();
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        } finally {
            targetDataLine.close();
            sourceDataLine.close();
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleSizeBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }
}


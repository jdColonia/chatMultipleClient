import java.io.ByteArrayInputStream;
import java.io.PrintWriter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class User {

    private String username; // Nombre de usuario del usuario
    PrintWriter out; // Flujo de salida para enviar mensajes al usuario

    public User(String username, PrintWriter out) {
        this.username = username;
        this.out = out;
    }

    // Obtiene el nombre de usuario del usuario
    public String getUsername() {
        return username;
    }

    // Obtiene el flujo de salida para enviar mensajes al usuario
    public PrintWriter getOut() {
        return out;
    }

    public void callUser(User receiver) {
        Call call = new Call(this, receiver);
    }

    public void receiveCall(User sender) {
    }

     // Método para recibir y reproducir una nota de voz
    public void receiveVoiceMessage(byte[] audioData) {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
            AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize());

            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(format);
            sourceDataLine.open(format);
            sourceDataLine.start();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }

            sourceDataLine.drain();
            sourceDataLine.stop();
            sourceDataLine.close();
            audioInputStream.close();
            byteArrayInputStream.close();
        } catch (Exception e) {
            System.err.println("Error al reproducir la nota de voz: " + e.getMessage());
        }
    }
}
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import javax.sound.sampled.AudioFormat;

public class User {
    private String username; // Nombre de usuario del usuario
    PrintWriter out; // Flujo de salida para enviar mensajes al usuario
    private ObjectOutputStream outputStream;

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

    public ObjectOutputStream getOutputStream(){
        return this.outputStream;
    }

    public void playAudio(byte[] audioData, AudioFormat format) {
        PlayerRecording player = new PlayerRecording(format);
        player.initiateAudio(audioData);
    }   
}
package server;
import java.io.PrintWriter;

import client.PlayerRecording;

public class User {
    private String username; // Nombre de usuario del usuario
    PrintWriter out; // Flujo de salida para enviar mensajes al usuario
    private PlayerRecording playerRecording;

    public User(String username, PrintWriter out) {
        this.username = username;
        this.out = out;
        this.playerRecording = new PlayerRecording();
    }

    // Obtiene el nombre de usuario del usuario
    public String getUsername() {
        return username;
    }

    // Obtiene el flujo de salida para enviar mensajes al usuario
    public PrintWriter getOut() {
        return out;
    }

    public PlayerRecording getPlayerRecording() {
        return playerRecording;
    }
}
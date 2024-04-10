package server;

import java.io.PrintWriter;

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
}
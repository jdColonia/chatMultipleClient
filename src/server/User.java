package server;

import java.io.PrintWriter;
import java.io.BufferedReader;

public class User {
    private String username; // Nombre de usuario del usuario
    PrintWriter out; // Flujo de salida para enviar mensajes al usuario
    private BufferedReader in; 

    public User(String username, PrintWriter out, BufferedReader in) {
        this.username = username;
        this.out = out;
        this.in = in;
    }

    // Obtiene el nombre de usuario del usuario
    public String getUsername() {
        return username;
    }

    // Obtiene el flujo de salida para enviar mensajes al usuario
    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }
}
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket clientSocket; // Socket para la conexión con el cliente
    private BufferedReader in; // Flujo de entrada para leer los mensajes del cliente
    private PrintWriter out; // Flujo de salida para enviar mensajes al cliente
    private String clientUsername; // Nombre de usuario del cliente
    ChatServer chat; // Objeto que contiene el chat

    public ClientHandler(Socket socket, ChatServer chat) {
        try {
            this.chat = chat;
            this.clientSocket = socket;
            // Crear canales de entrada in y de salida out para la comunicación
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            closeEveryThing(clientSocket, in, out);
        }
    }

    @Override
    public void run() {
        // Manejar el nombre de usuario del cliente
        handleClientUsername();
        while (clientSocket.isConnected()) {
            try {
                // Mensaje enviado por el usuario
                String messageFromClient = in.readLine();
                // Enviar el mensaje dependiendo del formato
                handleCommand(messageFromClient, clientUsername);
            } catch (IOException e) {
                closeEveryThing(clientSocket, in, out);
                break;
            }
        }
    }

    public void handleClientUsername() {
        // Obtiene el nombre de usuario que el cliente ha enviado
        try {
            clientUsername = in.readLine().trim();
            // Verifica que el nombre de usuario del nuevo cliente no exista
            while (!clientUsername.isEmpty() && chat.existUsr(clientUsername)) {
                out.println("[SERVIDOR] El nombre de usuario no está disponible. Por favor, ingresa otro nombre.");
                out.println("[SERVIDOR] Ingrese su nombre de usuario: ");
                clientUsername = in.readLine().trim();
            }
            synchronized (clientUsername) {
                chat.addUsr(clientUsername, out); // Añade al cliente al chat con su canal de salida out
                chat.broadcastMessage(clientUsername, "[SERVIDOR] " + clientUsername + " se ha unido al chat.");
                showInstructions(clientUsername);
            }
        } catch (IOException e) {
            closeEveryThing(clientSocket, in, out);
        }
    }

    public void showInstructions(String clientUsername) {
        out.println("----------------------------------------------------------------------------------------------");
        out.println("[SERVIDOR]:");
        out.println("Bienvenido/a al chat " + clientUsername + "!");
        out.println("Para enviar un mensaje a todos, solo escribe el mensaje y presiona Enter.");
        out.println("Para enviar un mensaje privado, escribe: /msg <usuario_destino> <mensaje>");
        out.println("Para enviar un mensaje a un grupo, escribe: /msggroup <nombre_grupo> <mensaje>");
        out.println("Para enviar un mensaje de voz privado, escribe: /voice <usuario_destino>");
        out.println("Para enviar un mensaje de voz a un grupo, escribe: /voicegroup <nombre_grupo>");
        out.println("Para crear un nuevo grupo, escribe: /creategroup <nombre_grupo>");
        out.println("Para unirte a un grupo, escribe: /join <nombre_grupo>");
        out.println("Para ver el historial de mensajes, escribe: /history");
        out.println("Para salir del chat, escribe: /exit");
        out.println("----------------------------------------------------------------------------------------------");
        out.flush();
    }

    public void handleCommand(String command, String sourceName) {
        String[] parts = command.split(" ", 3);
        switch (parts[0]) {
            case "/msg":
                chat.handleTextMessage(parts, sourceName);
                break;
            case "/msggroup":
                chat.handleGroupTextMessage(parts, sourceName);
                break;
            case "/voice":
                chat.handleVoiceMessage(parts, sourceName);
                break;
            case "/voicegroup":
                chat.handleGroupVoiceMessage(parts, sourceName);
                break;
            case "/creategroup":
                chat.handleCreateGroup(parts, sourceName);
                break;
            case "/join":
                chat.handleJoinGroup(parts, sourceName);
                break;
            case "/history":
                chat.showHistory(sourceName);
                break;
            default:
                chat.broadcastMessage(sourceName, "[" + sourceName + "]: " + command);
                break;
        }
    }

    public void closeEveryThing(Socket clientSocket, BufferedReader in, PrintWriter out) {
        System.out.println(clientUsername + " ha abandonado el chat.");
        chat.broadcastMessage("[SERVIDOR] " + clientUsername + " ha abandonado el chat.", clientUsername);
        chat.removeUsr(clientUsername);
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
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
        handleClientUsername(); // Manejar el nombre de usuario del cliente
    
        while (clientSocket.isConnected()) {
            try {
                String message = in.readLine(); // Leer el mensaje recibido del cliente
    
                if (message.startsWith("/voz")) {
                    // Procesar la nota de voz recibida
                    String[] parts = message.split(" ", 3);
                    String username = parts[1];
                    byte[] audioData = parseByteArray(parts[2]);
    
                    // Reproducir la nota de voz
                    chat.broadcastVoiceMessage(username, audioData); // Método a implementar en ChatServer
                } else {
                    // Procesar mensajes de texto normales
                    chat.handleCommand(message, clientUsername); // Manejar el comando recibido
                }
            } catch (IOException e) {
                closeEveryThing(clientSocket, in, out);
                break;
            }
        }
    }
    
    private byte[] parseByteArray(String arrayString) {
        // Convertir la cadena de bytes a un array de bytes
        String[] byteValues = arrayString.substring(1, arrayString.length() - 1).split(", ");
        byte[] bytes = new byte[byteValues.length];
        for (int i = 0; i < byteValues.length; i++) {
            bytes[i] = Byte.parseByte(byteValues[i]);
        }
        return bytes;
    }

    public void handleClientUsername() {
        // Obtiene el nombre de usuario que el cliente ha enviado
        try {
            clientUsername = in.readLine().trim();
            // Verifica que el nombre de usuario del nuevo cliente no exista
            while (!clientUsername.isEmpty() && chat.existUsr(clientUsername)) {
                out.println("[SERVIDOR] El nombre de usuario no está disponible. Por favor, ingresa otro nombre."); // Maneja el caso cuando el nombre de usuario ya existe
                out.println("[SERVIDOR] Ingrese su nombre de usuario: "); // Solicita de nuevo el nombre si ese nombre ya está en uso
                clientUsername = in.readLine().trim();
            }
            synchronized (clientUsername) {
                chat.addUsr(clientUsername, out); // Añade al cliente al chat con su canal de salida out
                chat.broadcastMessage(clientUsername, "[SERVIDOR] " + clientUsername + " se ha unido al chat."); // Notifica a los demás usuarios sobre el nuevo miembro
                showInstructions(clientUsername); // Muestra las instrucciones después de unirse al chat
            }
        } catch (IOException e) {
            closeEveryThing(clientSocket, in, out);
        }
    }

    public void showInstructions(String clientUsername) {
        out.println("----------------------------------------------------------------------------------------------");
        out.println("[SERVIDOR]:");
        out.println("¡Bienvenido al chat " + clientUsername + "!");
        out.println("Para enviar un mensaje a todos, solo escribe el mensaje y presiona Enter.");
        out.println("Para enviar un mensaje privado, escribe: /msg <usuario_destino> <mensaje>");
        out.println("Para enviar un mensaje a un grupo, escribe: /msggroup <nombre_grupo> <mensaje>");
        out.println("Para crear un nuevo grupo, escribe: /creategroup <nombre_grupo>");
        out.println("Para unirte a un grupo, escribe: /join <nombre_grupo>");
        out.println("Para ver el historial de mensajes, escribe: /history");
        out.println("Para salir del chat, escribe: /exit");
        out.println("----------------------------------------------------------------------------------------------");
        out.flush();
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
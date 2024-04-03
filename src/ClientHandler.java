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
        try {
            clientUsername = in.readLine().trim(); // Obtiene el nombre de usuario que el cliente ha enviado
            System.out.println(clientUsername);

            if (!clientUsername.isEmpty()) {
                synchronized (clientUsername) {
                    if (!chat.existUsr(clientUsername)) {
                        chat.addUsr(clientUsername, out); // Añade al cliente al chat con su canal de salida out
                        chat.broadcastMessage("[SERVIDOR] " + clientUsername + " se ha unido al chat.", clientUsername); // Notifica a los demás usuarios que hay un nuevo miembro en el chat
                        out.println("Bienvenido al chat, " + clientUsername + "!"); // Notifica al cliente que fue aceptado
                        showInstructions(); // Muestra las instrucciones después de unirse al chat
                    } else {
                        out.println("[SERVIDOR] El nombre de usuario ya está en uso. Por favor, ingresa otro nombre."); // Maneja el caso cuando el nombre de usuario ya existe
                        return; // Salir del método si el nombre de usuario ya existe
                    }
                }
            } else {
                out.println("[SERVIDOR] El nombre de usuario no puede estar vacío. Por favor, ingresa un nombre válido."); // Manejar el caso cuando el nombre de usuario está vacío
                return; // Salir del método si el nombre de usuario está vacío
            }

            // Mensaje enviado por el usuario
            String messageFromClient;
            // Esperar mensajes de cada cliente
            while ((messageFromClient = in.readLine()) != null) {
                // Enviar el mensaje dependiendo del formato
                chat.handleCommand(messageFromClient, clientUsername);
            }
        } catch (IOException e) {
            closeEveryThing(clientSocket, in, out);
        }
    }

    public void showInstructions() {
        out.println("----------------------------------------------------------------------------------------------");
        out.println("[SERVIDOR]:");
        out.println("¡Bienvenido al chat!");
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
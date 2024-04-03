import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {

    private Socket clientSocket; // Socket para la conexión con el cliente
    private BufferedReader in; // Flujo de entrada para leer los mensajes del cliente
    private PrintWriter out; // Flujo de salida para enviar mensajes al cliente
    private String clientUserName; // Nombre de usuario del cliente
    Chatters clients; // Objeto que contiene la lista de clientes conectados

    public ClientHandler(Socket socket, Chatters clients) {
        try {
            this.clients = clients;
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
            clientUserName = in.readLine().trim(); // Obtiene el nombre de usuario que el cliente ha enviado
    
            if (!clientUserName.isEmpty()) {
                synchronized (clientUserName) {
                    if (!clients.existUsr(clientUserName)) {
                        clients.addUsr(clientUserName, out); // Añade al cliente al chatters con su canal de salida out
                        clients.broadcastMessage("[SERVIDOR] " + clientUserName + " se ha unido al chat.", clientUserName); // Notifica a los demás usuarios que hay un nuevo miembro en el chat
                        out.println("Bienvenido al chat, " + clientUserName + "!"); // Notifica al cliente que fue aceptado
                    } else {
                        out.println("[SERVIDOR] El nombre de usuario ya está en uso. Por favor, ingresa otro nombre."); // Maneja el caso cuando el nombre de usuario ya existe
                        return; // Salir del método si el nombre de usuario ya existe
                    }
                }
            } else {
                out.println("[SERVIDOR] El nombre de usuario no puede estar vacío. Por favor, ingresa un nombre válido."); // Manejar el caso cuando el nombre de usuario está vacío
                return; // Salir del método si el nombre de usuario está vacío
            }
    
            String messageFromClient;
            // Intrucciones para el usuario


            // Esperar mensajes de cada cliente y enviarlo a todos los clientes
            while ((messageFromClient = in.readLine()) != null) {
                // Si el mensaje es dirigido a un cliente en especial, se debe separar el destinatario del mensaje y enviarlo únicamente a esa persona
                String[] parts = messageFromClient.split(":", 2);
                if (parts.length == 2) {
                    String recipient = parts[0].trim();
                    String content = parts[1].trim();
                    clients.sendPrivateMessage(clientUserName, recipient, content);
                } else {
                    clients.broadcastMessage(clientUserName + ": " + messageFromClient, clientUserName);
                }
    
            }
        } catch (IOException e) {
            closeEveryThing(clientSocket, in, out);
        }
    }
    

    public void closeEveryThing(Socket clientSocket, BufferedReader in, PrintWriter out) {
        System.out.println(clientUserName + " ha abandonado el chat.");
        clients.broadcastMessage("[SERVIDOR] " + clientUserName + " ha abandonado el chat.", clientUserName);
        clients.removeUsr(clientUserName);
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
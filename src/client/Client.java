package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static final BufferedReader CONSOLE_READER = new BufferedReader(new InputStreamReader(System.in));
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 3500;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private PrintWriter consoleOut;
    private String username;

    public Client(Socket clientSocket, String username) {
        try {
            this.clientSocket = clientSocket;
            this.username = username;
            // Crear canales de entrada in y de salida out para la comunicación
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.consoleOut = new PrintWriter(System.out, true);
        } catch (IOException e) {
            closeEveryThing(clientSocket, in, out, consoleOut);
        }
    }

    public void sendMessage() {
        out.println(username);
        out.flush();
        while (clientSocket.isConnected()) {
            try {
                String messageToSend = CONSOLE_READER.readLine();
                if (!messageToSend.isEmpty()) {
                    switch (messageToSend.split(" ")[0]) {
                        case "/voice":
                            
                            break;
                        case "stop":
                            
                            break;
                        case "/exit":
                            System.exit(0);
                            break;
                        default:
                            out.println(messageToSend);
                            out.flush();
                            break;
                    }
                } else {
                    System.out.println("[SERVIDOR] No se pueden enviar mensajes vacíos.");
                }
            } catch (IOException e) {
                closeEveryThing(clientSocket, in, out, consoleOut);
            }
        }
    }
    
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromServer;
                while (clientSocket.isConnected()) {
                    try {
                        msgFromServer = in.readLine();
                        consoleOut.println(msgFromServer);
                    } catch (IOException e) {
                        closeEveryThing(clientSocket, in, out, consoleOut);
                    }
                }
            }
        }).start();
    }

    public void closeEveryThing(Socket clientSocket, BufferedReader in, PrintWriter out, PrintWriter consoleOut) {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (consoleOut != null) {
                consoleOut.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("[SERVIDOR] Conectado al servidor");
        System.out.print("[SERVIDOR] Ingrese su nombre de usuario: ");
        String username = CONSOLE_READER.readLine();
        Socket socket = new Socket(SERVER_IP, PORT);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
}
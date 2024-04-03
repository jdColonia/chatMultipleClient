import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 6789;
    private ServerSocket serverSocket;
    ChatServer chat;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.chat = new ChatServer();
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Un nuevo cliente se ha conectado.");
                ClientHandler clientHandler = new ClientHandler(clientSocket, chat);
                // Crea el objeto Runable
                Thread thread = new Thread(clientHandler);
                // Inicia el hilo con el objeto Runnable
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Servidor iniciado. Esperando clientes...");
        ServerSocket serverSocket = new ServerSocket(PORT);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}
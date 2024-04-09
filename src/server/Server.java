package server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 3500;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    ChatServer chat;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.threadPool = Executors.newFixedThreadPool(10);
        this.chat = new ChatServer();
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Un nuevo cliente se ha conectado.");
                ClientHandler clientHandler = new ClientHandler(clientSocket, chat);
                threadPool.execute(clientHandler);
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
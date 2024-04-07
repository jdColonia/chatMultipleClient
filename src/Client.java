import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;


public class Client {

    public static final BufferedReader CONSOLE_READER = new BufferedReader(new InputStreamReader(System.in));
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 6789;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private PrintWriter consoleOut; // Nuevo PrintWriter para la consola
    private String username;

    public Client(Socket clientSocket, String username) {
        try {
            this.clientSocket = clientSocket;
            this.username = username;
            // Crear canales de entrada in y de salida out para la comunicación
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.consoleOut = new PrintWriter(System.out, true); // Inicializa el nuevo PrintWriter
        } catch (IOException e) {
            closeEveryThing(clientSocket, in, out);
        }
    }

    /*public void sendMessage() {
        out.println(username);
        out.flush();
        while (clientSocket.isConnected()) {
            try {
                String messageToSend = CONSOLE_READER.readLine(); // Lee desde la consola
                if (!messageToSend.isEmpty()) { // Verificar si el mensaje no está vacío
                    out.println(messageToSend);
                    out.flush();
                } else {
                    System.out.println("No se pueden enviar mensajes vacíos."); // Mostrar mensaje al usuario
                }
            } catch (IOException e) {
                closeEveryThing(clientSocket, in, out);
            }
        }
    }*/

    public void sendMessage() {
        out.println(username); // Envía el nombre de usuario al servidor
        out.flush();
    
        while (clientSocket.isConnected()) {
            try {
                String input = CONSOLE_READER.readLine(); // Lee la entrada del usuario desde la consola
    
                if (input.startsWith("/voz")) {
                    // Iniciar grabación de audio
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    AudioRecorderPlayer recorderPlayer = new AudioRecorderPlayer();
                    recorderPlayer.recordAudio(byteArrayOutputStream);
    
                    // Enviar los datos de audio al servidor
                    byte[] audioData = byteArrayOutputStream.toByteArray();
                    out.println("/voz " + username + " " + Arrays.toString(audioData));
                    out.flush();
                } else {
                    // Enviar mensaje de texto al servidor
                    if (!input.isEmpty()) {
                        out.println(input); // Envía el mensaje al servidor
                        out.flush();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error al enviar el mensaje: " + e.getMessage());
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
                        consoleOut.println(msgFromServer); // Imprime en la consola
                    } catch (IOException e) {
                        closeEveryThing(clientSocket, in, out);
                    }
                }
            }
        }).start();
    }

    public void closeEveryThing(Socket clientSocket, BufferedReader in, PrintWriter out) {
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Client {
    public static final BufferedReader CONSOLE_READER = new BufferedReader(new InputStreamReader(System.in));
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 6789;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectOutputStream objectOut;
    private PrintWriter consoleOut;
    private String username;

    public Client(Socket clientSocket, String username) {
        try {
            this.clientSocket = clientSocket;
            this.username = username;
            // Crear canales de entrada in y de salida out para la comunicación
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            this.consoleOut = new PrintWriter(System.out, true);
        } catch (IOException e) {
            closeEveryThing(clientSocket, in, out);
        }
    }

    public void sendMessage() {
        out.println(username);
        out.flush();
        while (clientSocket.isConnected()) {
            try {
                String messageToSend = CONSOLE_READER.readLine();
                if (!messageToSend.isEmpty()) {
                    out.println(messageToSend);
                    out.flush();
                } else {
                    System.out.println("[SERVIDOR] No se pueden enviar mensajes vacíos.");
                }
            } catch (IOException e) {
                closeEveryThing(clientSocket, in, out);
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
                        closeEveryThing(clientSocket, in, out);
                    }
                }
            }
        }).start();
    }

    public void startCall() throws IOException {
        System.out.println("Who are you calling?");
        String recipient = CONSOLE_READER.readLine();
        
        // Inicia la grabación de audio cuando el usuario presiona Enter
        System.out.print("\nPress Enter to start Talking...");
        CONSOLE_READER.readLine();
    
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
    
        if (!AudioSystem.isLineSupported(info)) {
            // Verifica si el sistema soporta la línea de entrada de audio
            System.err.println("Line not supported");
            return;
        }
    
        try (TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info)) {
            targetDataLine.open(audioFormat);
            targetDataLine.start();
    
            Thread recordingThread = new Thread(() -> {
                // escucha audio continuamente hasta que el usuario detiene la grabación
                int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
                byte[] buffer = new byte[bufferSize];
    
                while (true) {
                    int count = targetDataLine.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        try {
                            byteArrayOutputStream.write(buffer, 0, count);
                            VoiceCall call = new VoiceCall(recipient, username, byteArrayOutputStream.toByteArray());
                            objectOut.writeObject(call);
                            byteArrayOutputStream.reset();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            recordingThread.start();
    
            // Espera a que el usuario detenga la grabación
            System.out.print("Talk... Press Enter to finish call");
            CONSOLE_READER.readLine();
    
            // Detiene la grabación y cierra la línea de entrada de audio
            targetDataLine.stop();
            targetDataLine.close();
            recordingThread.interrupt();
            // Guarda el audio en un archivo y lo envía al servidor
    
            byteArrayOutputStream.close();
            objectOut.writeObject(new EndingFlag());
    
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
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
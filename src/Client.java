import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Client {
    public static final BufferedReader CONSOLE_READER = new BufferedReader(new InputStreamReader(System.in));
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 6789;
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
                        if (msgFromServer.equals("[AUDIO]")) {
                            handleAudioMessage();
                        } else {
                            consoleOut.println(msgFromServer);
                        }
                    } catch (IOException e) {
                        closeEveryThing(clientSocket, in, out);
                    }
                }
            }
        }).start();
    }

    private void handleAudioMessage() throws IOException {
        int sampleRate = Integer.parseInt(in.readLine());
        int sampleSizeInBits = Integer.parseInt(in.readLine());
        int channels = Integer.parseInt(in.readLine());
        boolean bigEndian = Boolean.parseBoolean(in.readLine());
        int audioDataLength = Integer.parseInt(in.readLine());
    
        String audioDataEncoded = in.readLine();
        byte[] audioData = Base64.getDecoder().decode(audioDataEncoded);
    
        if (audioData.length == audioDataLength) {
            AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, true, bigEndian);
            playAudio(audioData, format);
        } else {
            System.err.println("Error al recibir los datos de audio");
        }
    }
    
    private void playAudio(byte[] audioData, AudioFormat format) {
        try {
            AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length);
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);
            sourceDataLine.start();
    
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }
    
            sourceDataLine.drain();
            sourceDataLine.stop();
            sourceDataLine.close();
            audioInputStream.close();
        } catch (Exception e) {
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
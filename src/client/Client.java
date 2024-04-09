package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

public class Client {
    public static final BufferedReader CONSOLE_READER = new BufferedReader(new InputStreamReader(System.in));
    public static final PlayerRecording PLAYER_RECORDING = new PlayerRecording();
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
                    String[] parts = messageToSend.split(" ", 3);
                    switch (parts[0]) {
                        case "/voice":
                            System.out.println("[SERVIDOR] Grabando audio...");
                            System.out.println("[SERVIDOR] Ingrese 'stop' para detener la grabación.");
                            RecordAudio recordAudioPriv = new RecordAudio();
                            recordAudioPriv.startRecording();
                            String targetName = parts[1];
                            while (recordAudioPriv.isRecording()) {
                                String stopCommand = CONSOLE_READER.readLine();
                                if (stopCommand.equals("stop")) {
                                    recordAudioPriv.stopRecording();
                                    byte[] audioData = recordAudioPriv.getAudioData();
                                    out.println("/voice " + targetName + " " + Base64.getEncoder().encodeToString(audioData));
                                    out.flush();
                                    break;
                                }
                            }
                            break;
                        case "/voicegroup":
                            System.out.println("[SERVIDOR] Grabando audio...");
                            System.out.println("[SERVIDOR] Ingrese 'stop' para detener la grabación.");
                            RecordAudio recordAudioGroup = new RecordAudio();
                            recordAudioGroup.startRecording();
                            String groupName = parts[1];
                            while (recordAudioGroup.isRecording()) {
                                String stopCommand = CONSOLE_READER.readLine();
                                if (stopCommand.equals("stop")) {
                                    recordAudioGroup.stopRecording();
                                    byte[] audioData = recordAudioGroup.getAudioData();
                                    out.println("/voicegroup " + groupName + " " + Base64.getEncoder().encodeToString(audioData));
                                    out.flush();
                                    break;
                                }
                            }
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
                    System.out.print("[SERVIDOR] No se pueden enviar mensajes vacíos.");
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
                        if (msgFromServer.startsWith("/audiodata")) {
                            String audioDataStr = msgFromServer.substring("/audiodata ".length());
                            byte[] audioData = Base64.getDecoder().decode(audioDataStr);
                            PLAYER_RECORDING.initiateAudio(audioData);
                        } else {
                            consoleOut.println(msgFromServer);
                        }
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
package client;

public class cl {
        // public void startCall() throws IOException {
    //     System.out.println("Who are you calling?");
    //     String recipient = CONSOLE_READER.readLine();
        
    //     // Inicia la grabación de audio cuando el usuario presiona Enter
    //     System.out.print("\nPress Enter to start Talking...");
    //     CONSOLE_READER.readLine();
    
    //     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    //     AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
    //     DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
    
    //     if (!AudioSystem.isLineSupported(info)) {
    //         // Verifica si el sistema soporta la línea de entrada de audio
    //         System.err.println("Line not supported");
    //         return;
    //     }
    
    //     try (TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info)) {
    //         targetDataLine.open(audioFormat);
    //         targetDataLine.start();
    
    //         Thread recordingThread = new Thread(() -> {
    //             // escucha audio continuamente hasta que el usuario detiene la grabación
    //             int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
    //             byte[] buffer = new byte[bufferSize];
    
    //             while (true) {
    //                 int count = targetDataLine.read(buffer, 0, buffer.length);
    //                 if (count > 0) {
    //                     try {
    //                         byteArrayOutputStream.write(buffer, 0, count);
    //                         VoiceCall call = new VoiceCall(recipient, username, byteArrayOutputStream.toByteArray());
    //                         objectOut.writeObject(call);
    //                         byteArrayOutputStream.reset();
    //                     } catch (IOException e) {
    //                         e.printStackTrace();
    //                     }
    //                 }
    //             }
    //         });
    //         recordingThread.start();
    
    //         // Espera a que el usuario detenga la grabación
    //         System.out.print("Talk... Press Enter to finish call");
    //         CONSOLE_READER.readLine();
    
    //         // Detiene la grabación y cierra la línea de entrada de audio
    //         targetDataLine.stop();
    //         targetDataLine.close();
    //         recordingThread.interrupt();
    //         // Guarda el audio en un archivo y lo envía al servidor
    
    //         byteArrayOutputStream.close();
    //         objectOut.writeObject(new EndingFlag());
    
    //     } catch (LineUnavailableException | IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}

// import java.net.DatagramPacket;

// private class VoiceCallThread extends Thread {
//     @Override
//     public void run() {
//         try {
//             // Establecer la conexión de voz con el servidor
//             out.println("/call " + targetUsername);
//             out.flush();

//             // Enviar datos de audio al servidor
//             RecordAudio recordAudio = new RecordAudio();
//             recordAudio.startRecording();
//             while (recordAudio.isRecording()) {
//                 String stopCommand = CONSOLE_READER.readLine();
//                 if (stopCommand.equals("/callend")) {
//                     recordAudio.stopRecording();
//                     byte[] audioData = recordAudio.getAudioData();
//                     sendVoiceData(audioData);
//                     break;
//                 } else {
//                     byte[] audioData = recordAudio.getAudioData();
//                     sendVoiceData(audioData);
//                 }
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//     private void sendVoiceData(byte[] audioData) throws IOException {
//         byte[] packetData = ("/voicedata " + Base64.getEncoder().encodeToString(audioData)).getBytes();
//         DatagramPacket packet = new DatagramPacket(packetData, packetData.length, serverAddress, serverPort);
//         voiceSocket.send(packet);
//     }
// }
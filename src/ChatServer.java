import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioFormat;

public class ChatServer {
    private Map<String, User> users;
    private Map<String, Group> groups;
    private List<String> history;
    private Map<String, RecordAudio> recordingInstances;

    public ChatServer() {
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
        this.history = new ArrayList<>();
        this.recordingInstances = new HashMap<>();
    }

    // Verificar si un usuario existe en el conjunto de clientes
    public boolean existUsr(String username) {
        return users.containsKey(username);
    }

    // Agregar un usuario al conjunto de clientes
    public void addUsr(String username, PrintWriter out) {
        User user = new User(username, out);
        users.put(username, user);
    }

    // Eliminar un usuario del conjunto de clientes
    public void removeUsr(String username) {
        users.remove(username);
    }

    // Obtiene un usuario a través de su nombre
    public User getUser(String username) {
        return users.get(username);
    }

    // Obtiene un grupo
    public Group getGroup(String groupName) {
        return groups.get(groupName);
    }

    // Crea un grupo
    public void createGroup(String groupName, String creatorUsername) {
        Group group = new Group(groupName);
        groups.put(groupName, group);

        // Agregar al creador del grupo como miembro del grupo
        User creator = getUser(creatorUsername);
        if (creator != null) {
            group.addMember(creator);
            creator.getOut().println("[SERVIDOR] Grupo " + groupName + " creado exitosamente. Bienvenido/a al grupo!");
        }
    }

    // Añade a un usuario a un grupo
    public void joinGroup(String username, String groupName) {
        User user = getUser(username);
        Group group = getGroup(groupName);
        if (user != null && group != null) {
            if (!group.getMembers().contains(user)) {
                group.addMember(user);
                broadcastGroupMessage("[SERVIDOR] Bienvenido/a al grupo " + groupName + ", " + username + " se ha unido.", group);
            } else {
                user.getOut().println("[SERVIDOR] Ya eres miembro del grupo " + groupName);
            }
        } else {
            user.getOut().println("[SERVIDOR] El grupo " + groupName + " no existe");
        }
    }

    public void broadcastGroupMessage(String message, Group group) {
        for (User user : group.getMembers()) {
            user.getOut().println(message);
            user.getOut().flush();
        }
    }

    // Elimina un usuario de un grupo
    public void leaveGroup(String username, String groupName) {
        User user = getUser(username);
        Group group = getGroup(groupName);
        if (user != null && group != null) {
            group.removeMember(user);
        }
    }

    // Enviar un mensaje de broadcast a todos los usuarios excepto al remitente
    public void broadcastMessage(String sourceName, String message) {
        for (User user : users.values()) {
            if (!user.getUsername().equals(sourceName)) {
                user.getOut().println(message);
            }
        }
        history.add(message);
    }

        // Enviar un mensaje de broadcast a un remitente en especifico
    public void broadcastPrivateMessage(String sourceName, String message) {
        User user = users.get(sourceName);
        if (user != null) {
            user.getOut().println(message); // Envía el mensaje al usuario específico
            history.add(message);
        } else {
            System.out.println("El usuario '" + sourceName + "' no se encontró.");
        }
    }

        // Enviar un mensaje privado a la persona con un nombre dado
    public void sendPrivateMessage(String sourceName, String targetName, String message) {
        User source = getUser(sourceName);
        User target = getUser(targetName);
        if (source != null && target != null) {
            target.getOut().println("[Mensaje privado de " + sourceName + "]: " + message);
            history.add("[Mensaje privado de " + sourceName + " a " + targetName + "]: " + message);
        }
    }

    public void sendGroupMessage(String sourceName, String groupName, String message) {
        User source = getUser(sourceName);
        Group group = getGroup(groupName);
        if (source != null && group != null) {
            if (group.getMembers().contains(source)) {
                for (User user : group.getMembers()) {
                    if (!user.getUsername().equals(sourceName)) {
                        user.getOut().println("[Mensaje grupo " + groupName + " de " + sourceName + "]: " + message);
                    }
                }
                history.add("[Mensaje grupo " + groupName + " de " + sourceName + "]: " + message);
            } else {
                source.getOut().println("[SERVIDOR] No eres miembro del grupo " + groupName);
            }
        }
    }

    public void broadcastAudio(byte[] audioData, AudioFormat format) {
        for (User user : users.values()) {
            user.playAudio(audioData, format);
        }
    }

    public void sendPrivateAudio(User target, byte[] audioData, AudioFormat format) {
        target.playAudio(audioData, format);
    }

    public void handleCommand(String command, String sourceName) {
        String[] parts = command.split(" ", 3);
        switch (parts[0]) {
            case "/msg":
                handleTextMessage(parts, sourceName);
                break;
            case "/msggroup":
                handleGroupTextMessage(parts, sourceName);
                break;
            case "/voice":
                handleVoiceMessage(parts, sourceName);
                break;
            case "/voicegroup":
                handleGroupVoiceMessage(parts, sourceName);
                break;
            case "stop":
                handleStopRecording(sourceName);
                break;
            case "/creategroup":
                handleCreateGroup(parts, sourceName);
                break;
            case "/join":
                handleJoinGroup(parts, sourceName);
                break;
            case "/history":
                showHistory(sourceName);
                break;
            case "/exit":
                handleExit(sourceName);
                break;
            default:
                broadcastMessage(sourceName, "[" + sourceName + "]: " + command);
                break;
        }
    }

    private void handleTextMessage(String[] parts, String sourceName) {
        if (parts.length == 3) {
            String targetName = parts[1];
            String message = parts[2];
            sendPrivateMessage(sourceName, targetName, message);
        }
    }

    private void handleGroupTextMessage(String[] parts, String sourceName) {
        if (parts.length == 3) {
            String groupName = parts[1];
            String message = parts[2];
            sendGroupMessage(sourceName, groupName, message);
        }
    }

    private void handleVoiceMessage(String[] parts, String sourceName) {
        if (parts.length == 2) {
            String targetName = parts[1];
            User source = getUser(sourceName);
            RecordAudio recorder;
            if (targetName.equals("all")) {
                recorder = new RecordAudio(this, null);
            }
            if (!targetName.equals("all") && !targetName.equals(null)){
                recorder = new RecordAudio(this, targetName);
            }else {
                User target = getUser(targetName);
                if (target == null) {
                    source.getOut().println("[SERVIDOR] El usuario " + targetName + " no está conectado.");
                    return;
                }
                recorder = new RecordAudio(this, targetName);
            }
            recordingInstances.put(sourceName, recorder);
            source.getOut().println("[SERVIDOR] Grabando audio...");
            source.getOut().println("[SERVIDOR] Ingrese 'stop' para detener la grabación.");
            recorder.startRecording();
            history.add("[SERVIDOR] " + sourceName + " ha enviado un audio.");
        }
    }
    
    private void handleGroupVoiceMessage(String[] parts, String sourceName) {
        if (parts.length == 2) {
            String groupName = parts[1];
            User source = getUser(sourceName);
            Group group = getGroup(groupName);
            if (group == null) {
                source.getOut().println("[SERVIDOR] El grupo " + groupName + " no existe.");
                return;
            }
            if (!group.getMembers().contains(source)) {
                source.getOut().println("[SERVIDOR] No eres miembro del grupo " + groupName);
                return;
            }
            else{
                RecordAudio recorder = new RecordAudio(this, null);
                recordingInstances.put(sourceName, recorder);
                source.getOut().println("[SERVIDOR] Grabando audio para el grupo " + groupName + "...");
                source.getOut().println("[SERVIDOR] Ingrese 'stop' para detener la grabación.");
                recorder.startRecording();
                history.add("[SERVIDOR] " + sourceName + " ha enviado un audio a " + groupName + ".");
            }
        }
    }

    private void handleStopRecording(String sourceName) {
        User source = getUser(sourceName);
        RecordAudio recorder = recordingInstances.get(sourceName);
        if (recorder != null) {
            recorder.stopRecording();
            recordingInstances.remove(sourceName);
        } else {
            source.getOut().println("[SERVIDOR] No hay una grabación en curso.");
        }
    }

    private void handleCreateGroup(String[] parts, String sourceName) {
        if (parts.length == 2) {
            String groupName = parts[1];
            createGroup(groupName, sourceName);
        }
    }

    private void handleJoinGroup(String[] parts, String sourceName) {
        if (parts.length == 2) {
            String groupName = parts[1];
            joinGroup(sourceName, groupName);
        }
    }

    private void showHistory(String sourceName) {
        User user = getUser(sourceName);
        for (String message : history) {
            user.getOut().println(message);
        }
    }

    private void handleExit(String sourceName) {
        User user = getUser(sourceName);
        removeUsr(sourceName);
        user.getOut().println("¡Hasta luego!");
    }
}
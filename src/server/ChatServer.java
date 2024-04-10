package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {
    private Map<String, User> users;
    private Map<String, Group> groups;
    private List<String> history;

    public ChatServer() {
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
        this.history = new ArrayList<>();
    }

    // Verificar si un usuario existe en el conjunto de clientes
    public boolean existUsr(String username) {
        return users.containsKey(username);
    }

    // Agregar un usuario al conjunto de clientes
    public void addUsr(String username, PrintWriter out, BufferedReader in) {
        User user = new User(username, out, in);
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

    // Envia un mensaje a todos los usuarios del grupo
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

    public void sendPrivateAudio(String sourceName, String targetName, byte[] audioData) {
        User source = getUser(sourceName);
        User target = getUser(targetName);
        if (source != null && target != null) {
            String audioDataStr = "/audiodata " + Base64.getEncoder().encodeToString(audioData);
            target.getOut().println(audioDataStr);
            history.add("[SERVIDOR] " + sourceName + " ha enviado un audio.");
        }
    }
    
    public void sendGroupAudio(String sourceName, String groupName, byte[] audioData) {
        User source = getUser(sourceName);
        Group group = getGroup(groupName);
        String audioDataStr = "/audiodata " + Base64.getEncoder().encodeToString(audioData);
        if (source != null && group != null) {
            if (group.getMembers().contains(source)) {
                for (User user : group.getMembers()) {
                    if (!user.getUsername().equals(sourceName)) {
                        user.getOut().println(audioDataStr);
                    }
                }
                history.add("[SERVIDOR] " + sourceName + " ha enviado un audio a " + groupName + ".");
            } else {
                source.getOut().println("[SERVIDOR] No eres miembro del grupo " + groupName);
            }
        }
    }

    
    public void handleCall(String sourceName, String targetName) {
        User source = getUser(sourceName);
        User target = getUser(targetName);
    
        if (source != null && target != null) {
            // Notificar a los usuarios que se ha iniciado la llamada
            source.getOut().println("/callstarted " + targetName);
            target.getOut().println("/incomingcall " + sourceName);
    
            // Enviar la información necesaria para que el cliente inicie la llamada
            // source.getOut().println("/callsocketinfo " + target.getLocalPort());
            // target.getOut().println("/callsocketinfo " + source.getLocalPort());
        } else {
            if (source != null) {
                source.getOut().println("[SERVIDOR] El usuario " + targetName + " no está conectado.");
            }
        }
    }

    public void handleGroupCall(String sourceName, String groupName) {
        Group group = getGroup(groupName);
        if (group != null) {
            for (User user : group.getMembers()) {
                user.getOut().println("/callgroupstarted " + groupName);
            }
            // Aquí puedes agregar la lógica para establecer la comunicación de voz/video
        } else {
            getUser(sourceName).getOut().println("[SERVIDOR] El grupo " + groupName + " no existe.");
        }
    }
    
    // En caso de que no funcione en client
    public void handleCallEnd(String sourceName, String callTargetName) {
        User source = getUser(sourceName);
        User target = getUser(callTargetName);
        if (source != null && target != null) {
            source.getOut().println("[SERVIDOR] Llamada finalizada con: " + callTargetName);
            target.getOut().println("[SERVIDOR] Llamada finalizada.");
            // Aquí puedes agregar la lógica para finalizar la comunicación de voz/video
        } else {
            if (source != null) {
                source.getOut().println("[SERVIDOR] El usuario " + callTargetName + " no está conectado.");
            }
        }
    }

    public void handleGroupCallEnd(String sourceName, String callGroupName) {
        Group group = getGroup(callGroupName);
        if (group != null) {
            for (User user : group.getMembers()) {
                user.getOut().println("/callgroupend " + callGroupName);
            }
            // Aquí puedes agregar la lógica para finalizar la comunicación de voz/video grupal
        } else {
            getUser(sourceName).getOut().println("[SERVIDOR] El grupo " + callGroupName + " no existe.");
        }
    }
    


    public void handleTextMessage(String[] parts, String sourceName) {
        User source = getUser(sourceName);
        if (parts.length == 3) {
            String targetName = parts[1];
            String message = parts[2];
            User dest = getUser(targetName);
            if (dest != null){
                sendPrivateMessage(sourceName, targetName, message);
            }
            else{
                source.getOut().println("[SERVIDOR] El usuario " + targetName + " no existe.");
            }
        }
        else{
            source.getOut().println("[SERVIDOR] Error en el comando"); 
        }

    }

    public void handleGroupTextMessage(String[] parts, String sourceName) {
        User source = getUser(sourceName);
        String groupName = parts[1];
        if (parts.length == 3) {
            String message = parts[2];
            User dest = getUser(groupName);
            if (dest != null){
                sendGroupMessage(sourceName, groupName, message);
            }
            else{
                source.getOut().println("[SERVIDOR] El grupo " + groupName + " no existe.");
            }
        }
        else{
            source.getOut().println("[SERVIDOR] Error en el comando");
        }
    }

    public void handleCreateGroup(String[] parts, String sourceName) {
        if (parts.length == 2) {
            String groupName = parts[1];
            createGroup(groupName, sourceName);
        }
    }

    public void handleJoinGroup(String[] parts, String sourceName) {
        if (parts.length == 2) {
            String groupName = parts[1];
            joinGroup(sourceName, groupName);
        }
    }

    public void showHistory(String sourceName) {
        User user = getUser(sourceName);
        for (String message : history) {
            user.getOut().println(message);
        }
    }
}
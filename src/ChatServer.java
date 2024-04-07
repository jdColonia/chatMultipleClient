import java.io.PrintWriter;
import java.util.ArrayList;
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
            creator.getOut().println("[SERVIDOR] Grupo " + groupName + " creado exitosamente. ¡Bienvenido/a al grupo!");
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
            user.getOut().flush(); // Asegurar que el mensaje se envíe inmediatamente
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
    
    public void broadcastVoiceMessage(String username, byte[] audioData) {
        User user = getUser(username);
        if (user != null) {
            user.receiveVoiceMessage(audioData); // Llama al método para recibir y reproducir la nota de voz
        } else {
            System.err.println("Usuario no encontrado: " + username);
        }
    }
    
    // Método para transmitir una nota de voz a un grupo
    public void broadcastGroupVoiceMessage(String groupName, byte[] audioData) {
        Group group = getGroup(groupName);
        if (group != null) {
            for (User user : group.getMembers()) {
                user.receiveVoiceMessage(audioData); // Llama al método para recibir y reproducir la nota de voz
            }
        } else {
            System.err.println("Grupo no encontrado: " + groupName);
        }
    }

    public void showHistory(User user) {
        for (String message : history) {
            user.getOut().println(message);
        }
    }

    public void handleCommand(String command, String sourceName) {
        String[] parts = command.split(" ", 3);
        if (parts[0].equals("/msg")) {
            if (parts.length == 3) {
                String targetName = parts[1];
                String message = parts[2];
                sendPrivateMessage(sourceName, targetName, message);
            }
        } else if (parts[0].equals("/msggroup")) {
            if (parts.length == 3) {
                String groupName = parts[1];
                String message = parts[2];
                sendGroupMessage(sourceName, groupName, message);
            }
        } else if (parts[0].equals("/creategroup")) {
            if (parts.length == 2) {
                String groupName = parts[1];
                createGroup(groupName, sourceName);
            }
        } else if (parts[0].equals("/join")) {
            if (parts.length == 2) {
                String groupName = parts[1];
                joinGroup(sourceName, groupName);
            }
        } else if (parts[0].equals("/history")) {
            User user = getUser(sourceName);
            showHistory(user);
        } else if (parts[0].equals("/exit")) {
            User user = getUser(sourceName);
            removeUsr(sourceName);
            user.getOut().println("¡Hasta luego!");
        } else {
            broadcastMessage(sourceName, "[" + sourceName + "]: " + command);
        }
    }

}
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
    public void createGroup(String groupName) {
        Group group = new Group(groupName);
        groups.put(groupName, group);
    }

    // Añade a un usuario a un grupo
    public void joinGroup(String username, String groupName) {
        User user = getUser(username);
        Group group = getGroup(groupName);
        if (user != null && group != null) {
            group.addMember(user);
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
    public void broadcastMessage(String nameSrc, String message) {
        for (User user : users.values()) {
            if (!user.getUsername().equals(nameSrc)) {
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
            for (User user : group.getMembers()) {
                if (!user.getUsername().equals(sourceName)) {
                    user.getOut().println("[Mensaje grupo " + groupName + " de " + sourceName + "]: " + message);
                }
            }
            history.add("[Mensaje grupo " + groupName + " de " + sourceName + "]: " + message);
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
                createGroup(groupName);
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
            broadcastMessage(sourceName + ": " + command, sourceName);
        }
    }

}
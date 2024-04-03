import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Chatters {

    private Map<String, User> clients = new HashMap<>();

    // Verificar si un usuario existe en el conjunto de clientes
    public boolean existUsr(String name) {
        return clients.containsKey(name);
    }

    // Agregar un usuario al conjunto de clientes
    public void addUsr(String name, PrintWriter out) {
        User user = new User(name, out);
        clients.put(name, user);
    }

    // Eliminar un usuario del conjunto de clientes
    public void removeUsr(String name) {
        clients.remove(name);
    }

    // Enviar un mensaje de broadcast a todos los usuarios excepto al remitente
    public void broadcastMessage(String nameSrc, String message) {
        for (User user : clients.values()) {
            if (!user.getUsername().equals(nameSrc)) {
                user.getOut().println(message);
            }
        }
    }

    // Enviar un mensaje privado a la persona con un nombre dado nameDest
    public void sendPrivateMessage(String nameSrc, String nameDest, String message) {
        for (User user : clients.values()) {
            if (nameDest.equals(user.getUsername())) {
                user.getOut().println("[Chat privado de " + nameSrc + "]: " + message);
                break;
            } else {
                System.out.println("SERVIDOR: El usuario '" + nameDest + "' no está disponible.");
            }
        }
    }

}
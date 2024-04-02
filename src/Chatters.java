import java.util.Set;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Chatters {

    private Set<User> clients = new HashSet<>();

    public Chatters() {
    }

    public boolean existeUsr(String name) {
        boolean response = false;
        for (User p : clients) {
            if (name.equals(p.getUsername())) {
                response = true;
                break;
            }
        }
        return response;
    }

    public void addUsr(String name, PrintWriter out) {
        if (!name.isBlank() && !existeUsr(name)) {
            User p = new User(name, out);
            clients.add(p);
        }
    }

    public void removeUsr(String name) {
        for (User p : clients) {
            if (name.equals(p.getUsername())) {
                clients.remove(p);
                break;
            }
        }
    }

    public void broadcastMessage(String message) {

        for (User p : clients) {
            p.getOut().println(message);
        }
    }

    // Enviar un mensaje privado a la persona con un nombre dado nameDest
    public void sendPrivateMessage(String nameSrc, String nameDest, String message) {
        for (User p : clients) {
            if (nameDest.equals(p.getUsername())) {
                p.getOut().println("[Chat privado de " + nameSrc + "]: " + message);
                break;
            }
        }
    }

}
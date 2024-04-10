package server;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String nameGroup; // Nombre del grupo
    private List<User> members; // Lista de miembros del grupo

    public Group(String nameGroup) {
        this.nameGroup = nameGroup;
        this.members = new ArrayList<>();
    }

    // Obtiene el nombre del grupo
    public String getNameGroup() {
        return nameGroup;
    }

    // Obtiene los miembros del grupo
    public List<User> getMembers() {
        return members;
    }

    // Añade un miembro al grupo
    public void addMember(User user) {
        members.add(user);
    }

    // Elimina un miembro del grupo
    public void removeMember(User user) {
        members.remove(user);
    }
}
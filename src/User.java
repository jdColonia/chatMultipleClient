import java.io.PrintWriter;

public class User {

    private String username;
    PrintWriter out;

    public User(String username, PrintWriter out) {
        this.username = username;
        this.out = out;
    }

    public String getUsername() {
        return username;
    }

    public PrintWriter getOut() {
        return out;
    }

}
import java.io.BufferedReader;
import java.io.IOException;

public class Lector implements Runnable {

    String message;
    BufferedReader in;

    public Lector(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        // Leer la linea que envia el servidor e imprimir en pantalla
        try {

            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
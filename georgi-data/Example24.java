import java.io.*;

public class Example24 {
    public static void main(String[] args) {
        try {
            FileReader fr = new FileReader("nonexistentfile.txt");
            fr.read();
        } catch (IOException e) { }
        System.out.println("Continuing execution...");
    }
}
import java.io.FileInputStream;
import java.io.IOException;

public class Example3 {
    public static void readFile(String filename) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filename);
            int data;
            while ((data = fis.read()) != -1) {
                System.out.print((char) data);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                System.err.println("Error closing file: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        readFile("nonexistent_file.txt");
    }
}
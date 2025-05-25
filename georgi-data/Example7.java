import java.io.FileReader;
import java.io.IOException;

class Example7 {
    public void readFile() throws IOException {
        FileReader fr = new FileReader("file.txt");
        char[] buffer = new char[1024];
        fr.read(buffer);
    }
}
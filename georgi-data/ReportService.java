import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportService {
    public void generateDailyReport() {
        File file = new File("C:/reports/daily.txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Report content...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
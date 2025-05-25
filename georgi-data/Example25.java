import java.util.*;

class GodClass {
    private List<String> logs = new ArrayList<>();

    public void process() {
        readInput();
        compute();
        writeOutput();
        log("Process completed");
        notifyUser();
    }

    private void readInput() {
        System.out.println("Reading input...");
    }

    private void compute() {
        System.out.println("Computing...");
    }

    private void writeOutput() {
        System.out.println("Writing output...");
    }

    private void log(String message) {
        logs.add(message);
    }

    private void notifyUser() {
        System.out.println("User notified.");
    }
}
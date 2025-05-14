public class Logger {
    public void log(String level, String message) {
        if ("info".equals(level)) {
            System.out.println("INFO: " + message);
        } else if ("warn".equals(level)) {
            System.out.println("WARNING: " + message);
        } else if ("error".equals(level)) {
            System.err.println("ERROR: " + message);
        } else {
            System.out.println("UNKNOWN LEVEL");
        }
    }
}
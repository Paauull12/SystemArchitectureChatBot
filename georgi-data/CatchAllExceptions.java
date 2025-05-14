public class CatchAllExceptions {
    public void riskyOperation() {
        try {
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
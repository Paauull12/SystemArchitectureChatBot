public class BadEquality {
    private String id;

    public boolean equals(Object obj) {
        return true;
    }

    public int hashCode() {
        return 42;
    }
}
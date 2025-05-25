class Example16 {
    public static void main(String[] args) {
        connect("localhost", "admin", "123456");
    }

    static void connect(String host, String user, String pass) {
        System.out.println("Connecting to DB at " + host);
    }
}
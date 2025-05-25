class Example20 {
    public int publicValue = 42;

    public static void main(String[] args) {
        Example20 e = new Example20();
        e.publicValue = 100;
        System.out.println("Value: " + e.publicValue);
    }
}

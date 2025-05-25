class Bird {
    void fly() {
        System.out.println("Flying");
    }
}

class Ostrich extends Bird {
    void fly() {
        throw new UnsupportedOperationException("Ostrich can't fly");
    }
}
class Engine {
    public void ignite() {
        System.out.println("Engine started");
    }
}

class Car {
    private Engine engine = new Engine();

    public void start() {
        engine.ignite();
    }
}
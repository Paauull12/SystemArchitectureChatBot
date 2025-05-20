class Car {
    private Engine engine;

    public Car() {
        this.engine = new Engine();
    }

    public void startCar() {
        System.out.println("Car starting");
        engine.start();
    }
}
class Car {
    private Engine engine;

    public Car() {
        this.engine = new Engine(); // tightly coupled
    }

    public void startCar() {
        System.out.println("Car starting");
        engine.start();
    }
}
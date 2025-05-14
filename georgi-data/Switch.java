class Switch {
    private LightBulb bulb;

    public Switch() {
        bulb = new LightBulb(); // directly depends on concrete class
    }

    public void operate(String command) {
        if ("ON".equalsIgnoreCase(command)) {
            bulb.turnOn();
        } else {
            bulb.turnOff();
        }
    }
}
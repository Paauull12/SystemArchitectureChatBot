class Switch {
    private LightBulb bulb;

    public Switch() {
        bulb = new LightBulb();
    }

    public void operate(String command) {
        if ("ON".equalsIgnoreCase(command)) {
            bulb.turnOn();
        } else {
            bulb.turnOff();
        }
    }
}
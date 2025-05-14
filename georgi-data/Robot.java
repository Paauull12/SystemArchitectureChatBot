interface Worker {
    void work();
    void eat();
    void sleep();
}

class Robot implements Worker {
    public void work() {
        System.out.println("Robot working");
    }

    public void eat() {
        throw new UnsupportedOperationException("Robot doesn't eat");
    }

    public void sleep() {
        throw new UnsupportedOperationException("Robot doesn't sleep");
    }
}
class Animal {
    public void eat() {
        System.out.println("Eating...");
    }
}

class Dog extends Animal {
    public void bark() {
        System.out.println("Barking...");
    }
}

class RobotDog extends Dog {
    public void charge() {
        System.out.println("Charging battery...");
    }
}
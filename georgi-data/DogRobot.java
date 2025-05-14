class Animal {
    public void makeSound() {
        System.out.println("Some animal sound");
    }
}

class Dog extends Animal {
    public void fetchBall() {
        System.out.println("Dog fetching ball");
    }
}

class DogRobot extends Dog {
    @Override
    public void makeSound() {
        System.out.println("Beep boop bark");
    }

    public void selfDestruct() {
        System.out.println("DogRobot self-destructing...");
    }
}
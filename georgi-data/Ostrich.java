class Ostrich extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Ostriches can't fly");
    }

    @Override
    public void layEggs() {
        System.out.println("Laying eggs on the ground");
    }
}
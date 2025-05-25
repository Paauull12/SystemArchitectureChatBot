interface Worker {
    void work();
}

class SimpleWorker implements Worker {
    public void work() {
        System.out.println("Working...");
    }
}

class Example19 {
    public static void main(String[] args) {
        Worker worker = new SimpleWorker();
        worker.work();
    }
}
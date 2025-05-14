class Invoice {
    private String type;

    public Invoice(String type) {
        this.type = type;
    }

    public double calculateTotal() {
        if (type.equals("standard")) {
            return 100;
        } else if (type.equals("premium")) {
            return 200;
        } else if (type.equals("enterprise")) {
            return 500;
        }
        return 0;
    }
}
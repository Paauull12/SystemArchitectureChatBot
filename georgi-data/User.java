class User {
    private String name;
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void saveToDatabase() {
        System.out.println("Connecting to DB...");
        System.out.println("Saving user: " + name + ", Email: " + email);
        // DB logic here
    }

    public void sendWelcomeEmail() {
        System.out.println("Connecting to SMTP server...");
        System.out.println("Sending welcome email to " + email);
        // Email logic here
    }
}
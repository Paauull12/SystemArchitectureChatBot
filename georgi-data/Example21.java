public class Example21 {
    public static void main(String[] args) {
        User user = new User("Alice", null);
        System.out.println("User Email Length: " + user.getEmailLength());
    }
}

class User {
    private String name;
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public int getEmailLength() {
        return email.length();
    }

    public int getNameLength() {
        return name.length();
    }
}
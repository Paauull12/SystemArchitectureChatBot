public class ViolatesDRY {
    public void login(String username, String password) {
        if (username.equals("admin") && password.equals("admin123")) {
            System.out.println("Admin logged in");
        }
    }

    public void validate(String username, String password) {
        if (username.equals("admin") && password.equals("admin123")) {
            System.out.println("Valid credentials");
        }
    }
}
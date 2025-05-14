public class DuplicatedCode {
    public void printUserDetails(User user) {
        System.out.println("Name: " + user.getName());
        System.out.println("Email: " + user.getEmail());
    }

    public void printAdminDetails(Admin admin) {
        System.out.println("Name: " + admin.getName());
        System.out.println("Email: " + admin.getEmail());
    }
}

class Admin {
    private String name;
    private String email;
    public String getName() { return name; }
    public String getEmail() { return email; }
}
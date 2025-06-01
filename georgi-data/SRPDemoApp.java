import java.io.*;
import java.util.*;
import java.util.regex.*;

class User {
    private String username;
    private String email;
    private String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

class UserManager {
    private List<User> users = new ArrayList<>();
    private final File userFile = new File("users.txt");

    public void createUser(String username, String email, String password) {
        if (!validateEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }

        if (password.length() < 6) {
            System.out.println("Password too short.");
            return;
        }

        User user = new User(username, email, password);
        users.add(user);
        writeUserToFile(user);
        sendWelcomeEmail(email);
        System.out.println("User created: " + username);
    }

    private boolean validateEmail(String email) {
        System.out.println("Validating email: " + email);
        Pattern pattern = Pattern.compile("^\\S+@\\S+\\.\\S+$");
        return pattern.matcher(email).matches();
    }

    private void writeUserToFile(User user) {
        try (FileWriter fw = new FileWriter(userFile, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(user.getUsername() + "," + user.getEmail() + "," + user.getPassword());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Failed to write user to file.");
        }
    }

    private void sendWelcomeEmail(String email) {
        System.out.println("Sending welcome email to " + email);
    }

    public void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                users.add(new User(parts[0], parts[1], parts[2]));
            }
        } catch (IOException e) {
            System.out.println("Failed to load users.");
        }
    }

    public List<User> getUsers() {
        return users;
    }
}

public class SRPDemoApp {
    public static void main(String[] args) {
        UserManager manager = new UserManager();
        manager.loadUsers();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        manager.createUser(username, email, password);

        System.out.println("All users:");
        for (User u : manager.getUsers()) {
            System.out.println(" - " + u.getUsername() + " (" + u.getEmail() + ")");
        }
    }
}
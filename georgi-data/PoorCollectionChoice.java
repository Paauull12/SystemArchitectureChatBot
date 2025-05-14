import java.util.ArrayList;
import java.util.List;

public class PoorCollectionChoice {
    public void addUser(User user) {
        List<User> users = new ArrayList<>();
        if (!users.contains(user)) {
            users.add(user);
        }
    }
}
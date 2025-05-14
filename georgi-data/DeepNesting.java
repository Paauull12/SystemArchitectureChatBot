public class DeepNesting {
    public void process(User user) {
        if (user != null) {
            if (user.isActive()) {
                if (user.getProfile() != null) {
                    if (user.getProfile().isComplete()) {
                        System.out.println("User is ready");
                    }
                }
            }
        }
    }
}
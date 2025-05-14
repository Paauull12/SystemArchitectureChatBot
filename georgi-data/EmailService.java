public class EmailService {
    private final MailSender sender = new MailSender();

    public void sendEmail(String to, String content) {
        sender.send(to, content);
    }
}
package com.example.notifications;

public class EmailSender {

    public void sendEmail(String to, String subject, String body) {
        // Email sending logic using SMTP or external service
        System.out.println("Sending email to " + to);
    }

    public void sendWelcomeEmail(String to) {
        sendEmail(to, "Welcome!", "Thank you for signing up.");
    }
}

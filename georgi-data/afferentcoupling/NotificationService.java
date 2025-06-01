package com.example.notifications;

public class NotificationService {

    public void sendSMS(String phoneNumber, String message) {
        System.out.println("Sending SMS to " + phoneNumber + ": " + message);
    }

    public void sendPushNotification(String deviceId, String message) {
        System.out.println("Sending push notification to " + deviceId + ": " + message);
    }
}

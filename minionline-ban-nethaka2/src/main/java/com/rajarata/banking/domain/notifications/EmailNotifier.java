package com.rajarata.banking.domain.notifications;

public class EmailNotifier implements NotificationObserver {
    private String emailAddress;

    public EmailNotifier(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public void onNotification(String eventType, String message) {
        System.out.println("[EMAIL to " + emailAddress + "] Subject: " + eventType + " | Body: " + message);
    }
}

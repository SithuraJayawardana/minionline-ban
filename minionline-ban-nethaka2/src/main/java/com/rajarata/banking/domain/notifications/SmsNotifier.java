package com.rajarata.banking.domain.notifications;

public class SmsNotifier implements NotificationObserver {
    private String phoneNumber;

    public SmsNotifier(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void onNotification(String eventType, String message) {
        System.out.println("[SMS to " + phoneNumber + "] " + eventType + ": " + message);
    }
}

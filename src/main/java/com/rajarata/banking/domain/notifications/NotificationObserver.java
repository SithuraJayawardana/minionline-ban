package com.rajarata.banking.domain.notifications;

public interface NotificationObserver {
    void onNotification(String eventType, String message);
}

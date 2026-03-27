package com.rajarata.banking.domain.notifications;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private List<NotificationObserver> observers = new ArrayList<>();

    public void addObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyAll(String eventType, String message) {
        for (NotificationObserver observer : observers) {
            observer.onNotification(eventType, message);
        }
    }
}

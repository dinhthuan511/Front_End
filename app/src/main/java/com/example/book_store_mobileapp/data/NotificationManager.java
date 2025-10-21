package com.example.book_store_mobileapp.data;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private static NotificationManager instance;
    private List<AppNotification> notifications = new ArrayList<>();

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void addNotification(AppNotification notification) {
        notifications.add(0, notification); // Add to beginning of list
    }

    public void addPurchaseNotification(String bookTitle, double totalPrice) {
        AppNotification notification = new AppNotification(
            "purchase_" + System.currentTimeMillis(),
            "Purchase Successful",
            "You successfully purchased \"" + bookTitle + "\" for $" + String.format("%.2f", totalPrice),
            "purchase"
        );
        addNotification(notification);
    }

    public void addNewBookNotification(String bookTitle, String author) {
        AppNotification notification = new AppNotification(
            "new_book_" + System.currentTimeMillis(),
            "New Book Available",
            "\"" + bookTitle + "\" by " + author + " is now available in our store!",
            "new_book"
        );
        addNotification(notification);
    }

    public void addPromotionNotification(String promotionTitle, String description) {
        AppNotification notification = new AppNotification(
            "promotion_" + System.currentTimeMillis(),
            "Special Promotion",
            promotionTitle + ": " + description,
            "promotion"
        );
        addNotification(notification);
    }

    public List<AppNotification> getAllNotifications() {
        return notifications;
    }

    public List<AppNotification> getUnreadNotifications() {
        List<AppNotification> unread = new ArrayList<>();
        for (AppNotification notification : notifications) {
            if (!notification.isRead()) {
                unread.add(notification);
            }
        }
        return unread;
    }

    public int getUnreadCount() {
        return getUnreadNotifications().size();
    }

    public void markAsRead(String notificationId) {
        for (AppNotification notification : notifications) {
            if (notification.getId().equals(notificationId)) {
                notification.setRead(true);
                break;
            }
        }
    }

    public void markAllAsRead() {
        for (AppNotification notification : notifications) {
            notification.setRead(true);
        }
    }

    public void clearAllNotifications() {
        notifications.clear();
    }

    public void addSampleNotifications() {
        // Only add sample notifications if none exist
        if (notifications.isEmpty()) {
            addNewBookNotification("The Great Gatsby", "F. Scott Fitzgerald");
            addPromotionNotification("Summer Sale", "Get 20% off on all fiction books!");
            addPurchaseNotification("Clean Code", 15.99);
            addNewBookNotification("To Kill a Mockingbird", "Harper Lee");
            addPromotionNotification("New Arrivals", "Check out our latest collection of programming books");
        }
    }
}

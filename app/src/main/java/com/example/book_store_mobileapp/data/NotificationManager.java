package com.example.book_store_mobileapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.book_store_mobileapp.NotificationHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private static NotificationManager instance;
    private List<AppNotification> notifications = new ArrayList<>();

    private static final String PREFS_NAME = "app_notifications";
    private static final String KEY_NOTIFICATIONS = "notifications_list";

    private void saveNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_NOTIFICATIONS, new Gson().toJson(notifications))
                .apply();
    }

    private void loadNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_NOTIFICATIONS, null);
        if (json != null) {
            try {
                Type type = new TypeToken<List<AppNotification>>() {}.getType();
                notifications = new Gson().fromJson(json, type);
            } catch (Exception e) {
                notifications = new ArrayList<>();
            }
        }
    }
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public static NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager();
            instance.loadNotifications(context);
        }
        return instance;
    }

    // Keep original add (no system notification)
    public void addNotification(AppNotification notification) {
        notifications.add(0, notification); // Add to beginning of list
    }

    // Add notification with context (saves to SharedPreferences)
    public void addNotification(Context context, AppNotification notification) {
        addNotification(notification);
        saveNotifications(context);
    }

    public void addCartClearedNotification(Context context) {
        AppNotification notification = new AppNotification(
                "cart_cleared_" + System.currentTimeMillis(),
                "Cart Cleared",
                "You cleared all items from your cart.",
                "cart"
        );
        addNotification(context, notification);
    }


    public void addPurchaseNotification(Context context, String bookTitle, double totalPrice) {
        AppNotification notification = new AppNotification(
                "purchase_" + System.currentTimeMillis(),
                "Purchase Successful",
                "You purchased \"" + bookTitle + "\" for $" + String.format("%.2f", totalPrice),
                "purchase"
        );
        addNotification(context, notification);
    }

    public void addNewBookNotification(Context context, String bookTitle, String author) {
        AppNotification notification = new AppNotification(
                "new_book_" + System.currentTimeMillis(),
                "New Book Available",
                "\"" + bookTitle + "\" by " + author + " is now available in our store!",
                "new_book"
        );
        addNotification(context, notification);
    }

    public void addPromotionNotification(Context context, String promotionTitle, String description) {
        AppNotification notification = new AppNotification(
                "promotion_" + System.currentTimeMillis(),
                "Special Promotion",
                promotionTitle + ": " + description,
                "promotion"
        );
        addNotification(context, notification);
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

    public void addSampleNotifications(Context context) {
        // Only add sample notifications if none exist
        if (notifications.isEmpty()) {
            addNewBookNotification(context, "The Great Gatsby", "F. Scott Fitzgerald");
            addPromotionNotification(context, "Summer Sale", "Get 20% off on all fiction books!");
            addPurchaseNotification(context, "Clean Code", 15.99);
            addNewBookNotification(context, "To Kill a Mockingbird", "Harper Lee");
            addPromotionNotification(context, "New Arrivals", "Check out our latest collection of programming books");
        }
    }
}
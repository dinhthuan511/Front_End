package com.example.book_store_mobileapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationActivity extends Application {
    public static final String CHANNEL_ID_CART = "cart_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Cart Updates";
            String description = "Channel for cart badge updates";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID_CART, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);   // <- enables badge display

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}

package com.example.book_store_mobileapp.data;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.book_store_mobileapp.CartActivity;
import com.example.book_store_mobileapp.R;

public final class NotificationHelper {

    public static final String CART_CHANNEL_ID = "cart_channel_id";
    private static final int CART_NOTIFICATION_ID = 1001;

    private NotificationHelper() {}

    public static void createCartChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CART_CHANNEL_ID,
                    "Cart Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications related to your cart and purchases");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showCartNotification(Context context, int itemCount) {
        createCartChannel(context);

        Intent intent = new Intent(context, CartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "You have items in your cart";
        String text = "There are " + itemCount + " items waiting for you";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CART_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setNumber(itemCount) // Add number to external app badge
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(CART_NOTIFICATION_ID, builder.build());
    }

    public static void cancelCartNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(CART_NOTIFICATION_ID);
    }
}

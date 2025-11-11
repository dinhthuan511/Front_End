package com.example.book_store_mobileapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.book_store_mobileapp.network.FirebaseCartService;

public class NotificationHelper {

    private static final String CART_CHANNEL_ID = "cart_channel";
    public static final int CART_NOTIFICATION_ID = 1;

    // Create a single channel for all cart-related notifications
    public static void createCartChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Giỏ hàng";
            String description = "Thông báo cập nhật giỏ hàng";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CART_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Show system notification based on CART count (not notification count)
    public static void updateCartSystemNotification(Context context) {
        FirebaseCartService cartService = new FirebaseCartService();
        cartService.getCartItemCount(count -> {
            if (count > 0) {
                showCartNotification(context, count);
            } else {
                cancelCartNotification(context);
            }
        });
    }

    // Show a notification with cart count badge
    private static void showCartNotification(Context context, int cartItemCount) {
        Intent intent = new Intent(context, CartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Use proper Vietnamese grammar: "sách" (books)
        String itemText = cartItemCount == 1 ? "sách" : "sách";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CART_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_shopping_cart_black_24dp)
                .setContentTitle("Cập nhật giỏ hàng")
                .setContentText("Bạn đang có " + cartItemCount + " " + itemText + " trong giỏ hàng")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setNumber(cartItemCount) // Badge shows unique item count
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(CART_NOTIFICATION_ID, builder.build());
    }

    // Cancel the cart notification
    public static void cancelCartNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(CART_NOTIFICATION_ID);
    }
}

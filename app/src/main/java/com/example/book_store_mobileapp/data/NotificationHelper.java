package com.example.book_store_mobileapp.data;

import android.content.Context;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.book_store_mobileapp.NotificationActivity;
import com.example.book_store_mobileapp.R;

public class NotificationHelper {

    public static void showCartBadge(Context context, int cartCount) {
        // Create a lightweight "silent" notification whose only purpose is badge update.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, NotificationActivity.CHANNEL_ID_CART)
                        .setSmallIcon(R.drawable.ic_shopping_cart)   // use existing cart icon
                        .setContentTitle("Cart updated")
                        .setContentText("You have " + cartCount + " items in your cart")
                        .setAutoCancel(true)
                        .setSilent(true)
                        .setNumber(cartCount)      // <-- critical for badge number
                        .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1001, builder.build());
    }
}

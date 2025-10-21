package com.example.book_store_mobileapp.data;

import android.content.Context;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.book_store_mobileapp.NotificationActivity;
import com.example.book_store_mobileapp.R;
import java.util.List;

public class NotificationHelper {

    // preferred for direct cart count
    public static void showCartNotification(Context context, int cartCount) {
        buildNotification(context, cartCount);
    }

    //accepts List<CartItem>
    public static void showCartNotification(Context context, List<CartItem> cartItems) {
        int cartCount = (cartItems != null) ? cartItems.size() : 0;
        buildNotification(context, cartCount);
    }


    private static void buildNotification(Context context, int count) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, NotificationActivity.CHANNEL_ID_CART)
                        .setSmallIcon(R.drawable.ic_shopping_cart)
                        .setContentTitle("Cập nhật giỏ hàng")
                        .setContentText("Bạn có " + count + " sản phẩm trong giỏ hàng.")
                        .setAutoCancel(true)
                        .setSilent(true)
                        .setNumber(count)
                        .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1001, builder.build());
    }
}

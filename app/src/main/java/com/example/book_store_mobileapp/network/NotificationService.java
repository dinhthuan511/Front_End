package com.example.book_store_mobileapp.network;

import android.util.Log;
import com.example.book_store_mobileapp.data.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

public class NotificationService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseCartBadge";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token: " + token);
        // Optionally send token to backend if needed later
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String type = data.get("type");
            if ("cart_update".equals(type)) {
                try {
                    int count = Integer.parseInt(data.get("cart_count"));
                    NotificationHelper.showCartNotification(getApplicationContext(), count);
                } catch (Exception e) {
                    Log.e(TAG, "Invalid cart_count", e);
                }
            }
        }
    }
}

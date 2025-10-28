package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.book_store_mobileapp.data.NotificationManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    private BadgeDrawable notificationBadge;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        // Sử dụng layout cơ sở activity_base.xml
        super.setContentView(R.layout.activity_base);

        // Ánh xạ FrameLayout và BottomNavigationView từ layout cơ sở
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Cho layout của Activity con vào FrameLayout
        getLayoutInflater().inflate(layoutResID, contentFrame, true);

        // Thiết lập listener cho BottomNavigationView
        setupBottomNavigation();

        // 🔹 Setup notification badge
        setupNotificationBadge();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Nếu người dùng nhấn vào mục đang được chọn thì không làm gì
            if (itemId == bottomNavigationView.getSelectedItemId()) {
                return false;
            }

            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(this, StoreActivity.class);
            } else if (itemId == R.id.nav_cart_bottom) {
                intent = new Intent(this, CartActivity.class);
            } else if (itemId == R.id.nav_notifications) {
                intent = new Intent(this, NotificationCenterActivity.class);
            } else if (itemId == R.id.nav_profile) {
                // intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                // Cải tiến quan trọng: Dùng flag để không tạo lại Activity đã có
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }

            // Return true để mục được chọn hiển thị là active
            return true;
        });
    }

    /**
     * 🔸 Create and initialize the red badge on the notification icon
     */
    private void setupNotificationBadge() {
        notificationBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifications);
        notificationBadge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        notificationBadge.setBadgeTextColor(getResources().getColor(android.R.color.white));
        notificationBadge.setVisible(false); // start hidden
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật mục được chọn trên BottomNav dựa trên Activity hiện tại
        updateNavigationBarState();
        updateNotificationBadge();
    }

    /**
     // Phương thức này sẽ được các Activity con override để cho biết mục nào cần highlight
     * 🔸 Update the notification badge count based on unread notifications
     */
    protected void updateNotificationBadge() {
        NotificationManager manager = NotificationManager.getInstance();
        int unreadCount = manager.getUnreadCount();
        if (unreadCount > 0) {
            notificationBadge.setVisible(true);
            notificationBadge.setNumber(unreadCount);
        } else {
            notificationBadge.clearNumber();
            notificationBadge.setVisible(false);
        }
    }

    // Phương thức này sẽ được các Activity con override để cho biết mục nào cần highlight
    protected abstract int getNavigationMenuItemId();

    private void updateNavigationBarState() {
        int menuItemId = getNavigationMenuItemId();
        bottomNavigationView.setSelectedItemId(menuItemId);
    }
}

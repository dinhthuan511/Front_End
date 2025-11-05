package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.book_store_mobileapp.data.NotificationManager;
import com.example.book_store_mobileapp.ui.auth.LoginActivity;
import com.example.book_store_mobileapp.ui.auth.ProfileActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    private BadgeDrawable notificationBadge;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        // Load layout cha trước
        super.setContentView(R.layout.activity_base);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load layout con vào khung chứa
        getLayoutInflater().inflate(layoutResID, contentFrame, true);

        setupBottomNavigation();
        setupNotificationBadge(); // ✅ Khởi tạo badge tại đây (trước onResume)
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == bottomNavigationView.getSelectedItemId()) return false;

            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (itemId == R.id.nav_store) {
                intent = new Intent(this, StoreActivity.class);
            } else if (itemId == R.id.nav_notifications) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    intent = new Intent(this, NotificationCenterActivity.class);
                } else {
                    intent = new Intent(this, LoginActivity.class);
                }
            } else if (itemId == R.id.nav_profile) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    intent = new Intent(this, ProfileActivity.class);
                } else {
                    intent = new Intent(this, LoginActivity.class);
                }
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            return true;
        });
    }

    private void setupNotificationBadge() {
        // ✅ Luôn dùng getOrCreateBadge() để không bao giờ null
        notificationBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifications);
        notificationBadge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        notificationBadge.setBadgeTextColor(getResources().getColor(android.R.color.white));
        notificationBadge.setVisible(false); // ẩn mặc định
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationBarState();
        updateNotificationBadge();
    }

    protected void updateNotificationBadge() {
        if (notificationBadge == null) {
            // ✅ Phòng ngừa nếu vì lý do nào đó chưa init
            setupNotificationBadge();
        }

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

    protected abstract int getNavigationMenuItemId();

    private void updateNavigationBarState() {
        bottomNavigationView.setSelectedItemId(getNavigationMenuItemId());
    }
}

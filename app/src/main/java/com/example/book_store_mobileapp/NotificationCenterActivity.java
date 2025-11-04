package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.book_store_mobileapp.adapter.NotificationAdapter;
import com.example.book_store_mobileapp.data.AppNotification;
import com.example.book_store_mobileapp.data.NotificationManager;

import java.util.List;

public class NotificationCenterActivity extends AppCompatActivity {

    private ListView listViewNotifications;
    private LinearLayout emptyState;
    private Button btnMarkAllRead;
    private NotificationAdapter adapter;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        listViewNotifications = findViewById(R.id.listViewNotifications);
        emptyState = findViewById(R.id.emptyState);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        ImageButton btnBack = findViewById(R.id.btnBack);

        notificationManager = NotificationManager.getInstance();

        // Back button
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationCenterActivity.this, StoreActivity.class);
            startActivity(intent);
            finish();
        });

        // Mark all as read button
        btnMarkAllRead.setOnClickListener(v -> {
            notificationManager.markAllAsRead();
            // Clear system notification/badge
            NotificationHelper.cancelCartNotification(NotificationCenterActivity.this);
            updateNotificationList();
        });

        // Set up list view
        setupNotificationList();

        // Load notifications
        updateNotificationList();
    }

    private void setupNotificationList() {
        List<AppNotification> notifications = notificationManager.getAllNotifications();
        adapter = new NotificationAdapter(this, notifications);
        listViewNotifications.setAdapter(adapter);

        // Handle notification click
        listViewNotifications.setOnItemClickListener((parent, view, position, id) -> {
            AppNotification notification = notifications.get(position);
            if (!notification.isRead()) {
                notificationManager.markAsRead(notification.getId());
                updateNotificationList();
            }
        });
    }

    private void updateNotificationList() {
        List<AppNotification> notifications = notificationManager.getAllNotifications();

        if (notifications.isEmpty()) {
            listViewNotifications.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            listViewNotifications.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationList();
        // If no unread -> cancel system badge
        if (notificationManager.getUnreadCount() == 0) {
            NotificationHelper.cancelCartNotification(this);
        }
    }
}


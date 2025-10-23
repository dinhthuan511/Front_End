package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    // Giữ listener riêng để có thể tháo/gắn tạm thời
    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();

        // Nhấn lại đúng tab đang chọn thì thôi
        if (itemId == bottomNavigationView.getSelectedItemId()) return false;

        Intent intent = null;
        if (itemId == R.id.nav_home) {
            intent = new Intent(this, StoreActivity.class);
        } else if (itemId == R.id.nav_cart_bottom) {
            intent = new Intent(this, CartActivity.class);
        } else if (itemId == R.id.nav_notifications) {
            // intent = new Intent(this, NotificationActivity.class);
        } else if (itemId == R.id.nav_profile) {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                intent = new Intent(this, SettingsActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        return true;
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        // Dùng layout khung
        super.setContentView(R.layout.activity_base);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Bơm layout con vào khung
        getLayoutInflater().inflate(layoutResID, contentFrame, true);

        // Gắn listener một lần
        bottomNavigationView.setOnItemSelectedListener(navListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationBarState();
    }

    // Mỗi Activity con trả về id item cần highlight
    protected abstract int getNavigationMenuItemId();

    private void updateNavigationBarState() {
        int menuItemId = getNavigationMenuItemId();

        // Ngăn không cho listener chạy khi chỉ muốn "đánh dấu"
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(menuItemId);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        // (Hoặc dùng dòng dưới thay cho 3 dòng trên:
        // bottomNavigationView.getMenu().findItem(menuItemId).setChecked(true);
        // nếu bạn thích cách đơn giản hơn.)
    }
}

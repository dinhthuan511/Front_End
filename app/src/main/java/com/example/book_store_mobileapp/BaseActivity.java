package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.book_store_mobileapp.ui.auth.LoginActivity;
import com.example.book_store_mobileapp.ui.auth.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    // Listener dùng chung, có thể tháo/gắn tạm để tránh trigger khi chỉ muốn "đánh dấu" tab
    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();

        // Nếu nhấn lại đúng tab đang chọn thì bỏ qua
        if (itemId == bottomNavigationView.getSelectedItemId()) return false;

        Intent intent = null;

        if (itemId == R.id.nav_home) {
            intent = new Intent(this, HomeActivity.class);
        } else if (itemId == R.id.nav_store) {
            intent = new Intent(this, StoreActivity.class);
        } else if (itemId == R.id.nav_cart_bottom) {
            intent = new Intent(this, CartActivity.class);
        } else if (itemId == R.id.nav_notifications) {
            // TODO: mở NotificationActivity nếu có
            // intent = new Intent(this, NotificationActivity.class);
        } else if (itemId == R.id.nav_profile) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                intent = new Intent(this, ProfileActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
        }

        if (intent != null) {
            // Không tạo thêm instance nếu activity đã có trong back stack
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            // Tắt animation cho cảm giác chuyển tab tức thì
            overridePendingTransition(0, 0);
        }
        // Trả về true để item hiển thị active
        return true;
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        // Dùng layout khung activity_base.xml
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

    // Mỗi Activity con trả về id của item cần highlight (ví dụ: R.id.nav_home)
    protected abstract int getNavigationMenuItemId();

    private void updateNavigationBarState() {
        int menuItemId = getNavigationMenuItemId();

        if (menuItemId == -1) {
            // ❌ Không highlight gì và ẨN nav bar
            bottomNavigationView.setVisibility(View.GONE);
            return;
        }

        // ✅ Nếu có nav -> HIỆN nav
        bottomNavigationView.setVisibility(View.VISIBLE);

        // Tháo listener để set chọn mà không bị điều hướng
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(menuItemId);
        // Gắn lại listener
        bottomNavigationView.setOnItemSelectedListener(navListener);
    }

}

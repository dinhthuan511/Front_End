package com.example.book_store_mobileapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//import com.example.book_store_mobileapp.AddressActivity;
import com.example.book_store_mobileapp.OrderListActivity;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAuthService; // ✅ service
import com.google.android.material.appbar.MaterialToolbar;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "SETTINGS_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1) Thiết lập Toolbar làm ActionBar để tiêu đề hiển thị ổn định
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        if (bar != null) {
            setSupportActionBar(bar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Tài khoản của tôi");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            bar.setNavigationOnClickListener(v -> finish());
        } else {
            Log.e(TAG, "topAppBar == null");
        }

        View rowAccount = findViewById(R.id.rowAccountSecurity);
        if (rowAccount != null) {
            ((TextView) rowAccount.findViewById(R.id.title)).setText("Tài khoản & bảo mật");
            ((ImageView) rowAccount.findViewById(R.id.icon)).setImageResource(R.drawable.outline_house_with_shield_24);

            rowAccount.setOnClickListener(v ->
                    startActivity(new Intent(this, AccountSecurityActivity.class))
            );
        }

// 3) Row: Xem đơn hàng của tôi
        View rowMyOrders = findViewById(R.id.rowMyOrders);
        if (rowMyOrders != null) {
            TextView title = rowMyOrders.findViewById(R.id.title);
            ImageView icon = rowMyOrders.findViewById(R.id.icon);

            if (title != null) title.setText("Xem đơn hàng của tôi");
            if (icon != null) icon.setImageResource(R.drawable.outline_orders_24); // 🔹 icon hóa đơn

            // Khi bấm thì mở trang xem đơn hàng
            rowMyOrders.setOnClickListener(v -> {
                Intent intent = new Intent(                     this, OrderListActivity.class);
                // ⚠️ Nếu bạn có danh sách đơn hàng riêng, thay bằng OrderListActivity.class
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "Không tìm thấy rowMyOrders");
        }


        // 4) Logout (qua service)
        View btnLogoutBottom = findViewById(R.id.btnLogoutBottom);
        if (btnLogoutBottom != null) {
            btnLogoutBottom.setOnClickListener(v -> {
                // ✅ chỉ 1 dòng
                new FirebaseAuthService().logout();
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            });
        }


    }
}
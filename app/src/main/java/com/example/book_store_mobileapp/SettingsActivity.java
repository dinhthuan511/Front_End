package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SETTINGS_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        // 2) Row: Tài khoản & Bảo mật
        // CHÚ Ý: id đặt trên <include> CHÍNH LÀ root của item đã include
        View rowAccount = findViewById(R.id.rowAccountSecurity);
        if (rowAccount != null) {
            TextView title = rowAccount.findViewById(R.id.title);
            ImageView icon = rowAccount.findViewById(R.id.icon);

            if (title != null) title.setText("Tài khoản & bảo mật");
            if (icon != null) icon.setImageResource(R.drawable.outline_house_with_shield_24);

            // GẮN CLICK trực tiếp vào rowAccount (root của item include)
            rowAccount.setOnClickListener(v ->
                    startActivity(new Intent(this, AccountSecurityActivity.class)));
        } else {
            Log.e(TAG, "Không tìm thấy rowAccountSecurity");
        }

        // 3) Row: Địa chỉ
        View rowAddress = findViewById(R.id.rowAddress);
        if (rowAddress != null) {
            TextView title = rowAddress.findViewById(R.id.title);
            ImageView icon = rowAddress.findViewById(R.id.icon);

            if (title != null) title.setText("Địa chỉ");
            if (icon != null) icon.setImageResource(R.drawable.outline_location_on_24);

            rowAddress.setOnClickListener(v ->
                    startActivity(new Intent(this, AddressActivity.class)));
        } else {
            Log.e(TAG, "Không tìm thấy rowAddress");
        }

        // 4) Logout
        View btnLogoutBottom = findViewById(R.id.btnLogoutBottom);
        if (btnLogoutBottom != null) {
            btnLogoutBottom.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            });
        }
    }
}

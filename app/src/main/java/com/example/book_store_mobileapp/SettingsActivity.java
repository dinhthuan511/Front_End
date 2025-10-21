package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Toolbar: tiêu đề + nút back
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        if (bar != null) {
            bar.setTitle("Tài khoản của tôi");
            bar.setNavigationOnClickListener(v -> finish());
        }

        // Row 1: Tài khoản & bảo mật
        View rowAccount = findViewById(R.id.rowAccountSecurity);
        if (rowAccount != null) {
            ((TextView) rowAccount.findViewById(R.id.title)).setText("Tài khoản & bảo mật");
            ((ImageView) rowAccount.findViewById(R.id.icon))
                    .setImageResource(R.drawable.outline_house_with_shield_24);
            rowAccount.setOnClickListener(v ->
                    startActivity(new Intent(this, AccountSecurityActivity.class)));
        }

        // Row 2: Địa chỉ
        View rowAddress = findViewById(R.id.rowAddress);
        if (rowAddress != null) {
            ((TextView) rowAddress.findViewById(R.id.title)).setText("Địa chỉ");
            ((ImageView) rowAddress.findViewById(R.id.icon))
                    .setImageResource(R.drawable.outline_location_on_24);
            rowAddress.setOnClickListener(v ->
                    startActivity(new Intent(this, AddressActivity.class)));
        }
        View btnLogoutBottom = findViewById(R.id.btnLogoutBottom);
        if (btnLogoutBottom != null) {
            btnLogoutBottom.setOnClickListener(v -> {
                // Đăng xuất Firebase
                FirebaseAuth.getInstance().signOut();

                // Quay lại LoginActivity, xóa lịch sử stack
                Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            });
        }
    }
}

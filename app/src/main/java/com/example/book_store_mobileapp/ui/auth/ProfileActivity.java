package com.example.book_store_mobileapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.book_store_mobileapp.AddressActivity;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAuthService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Màn Profile (Tài khoản của tôi)
 * - Show greeting: "Xin chào, username"
 * - Điều hướng đến các màn quản lý account
 * - Logout
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "PROFILE_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ✅ Setup toolbar & back button
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        if (bar != null) {
            setSupportActionBar(bar);
            if (getSupportActionBar() != null) {
                // ❌ Không set title cứng nữa
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            bar.setNavigationOnClickListener(v -> finish());
        } else {
            Log.e(TAG, "topAppBar == null");
        }

        // ✅ Đặt tiêu đề động: "Xin chào, username"
        setGreetingTitle();

        // ✅ Row: Tài khoản & bảo mật
        View rowAccount = findViewById(R.id.rowAccountSecurity);
        if (rowAccount != null) {
            TextView title = rowAccount.findViewById(R.id.title);
            ImageView icon = rowAccount.findViewById(R.id.icon);

            if (title != null) title.setText("Tài khoản & bảo mật");
            if (icon != null) icon.setImageResource(R.drawable.outline_house_with_shield_24);

            rowAccount.setOnClickListener(v ->
                    startActivity(new Intent(this, AccountSecurityActivity.class)));
        }

        // ✅ Row: Địa chỉ
        View rowAddress = findViewById(R.id.rowAddress);
        if (rowAddress != null) {
            TextView title = rowAddress.findViewById(R.id.title);
            ImageView icon = rowAddress.findViewById(R.id.icon);

            if (title != null) title.setText("Địa chỉ");
            if (icon != null) icon.setImageResource(R.drawable.outline_location_on_24);

            rowAddress.setOnClickListener(v ->
                    startActivity(new Intent(this, AddressActivity.class)));
        }

        // ✅ Logout
        View btnLogoutBottom = findViewById(R.id.btnLogoutBottom);
        if (btnLogoutBottom != null) {
            btnLogoutBottom.setOnClickListener(v -> {
                new FirebaseAuthService().logout();
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            });
        }
    }

    /**
     * ✅ Lấy username từ Firestore, fallback email
     * ✅ Set title: "Xin chào, {username}"
     */
    private void setGreetingTitle() {
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (bar == null) return;

        if (user == null) {
            bar.setTitle("Xin chào bạn");
            return;
        }

        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String name = (doc != null) ? doc.getString("username") : null;

                    // Nếu user chưa có username → fallback tên email
                    if (TextUtils.isEmpty(name)) {
                        String email = user.getEmail();
                        if (!TextUtils.isEmpty(email) && email.contains("@")) {
                            name = email.substring(0, email.indexOf('@'));
                        } else {
                            name = "bạn";
                        }
                    }

                    bar.setTitle("Xin chào, " + name);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi lấy username: ", e);
                    bar.setTitle("Xin chào bạn");
                });
    }
}

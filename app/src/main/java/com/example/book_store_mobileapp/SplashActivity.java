package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.book_store_mobileapp.ui.admin.AdminDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.concurrent.atomic.AtomicBoolean;

public class SplashActivity extends AppCompatActivity {

    private final AtomicBoolean navigated = new AtomicBoolean(false);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Hiển thị splash theo theme (nhẹ, hệ thống quản lý animation)
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.w("ROUTER", "Guest user -> StoreActivity");
            go(StoreActivity.class);
            return;
        }

        // ✅ Ép refresh token để đảm bảo claim admin mới nhất luôn chính xác
        user.getIdToken(true)
                .addOnSuccessListener((GetTokenResult result) -> {
                    boolean isAdmin = Boolean.TRUE.equals(result.getClaims().get("admin"));
                    Log.w("ROUTER", "Splash route. isAdmin=" + isAdmin);
                    go(isAdmin ? AdminDashboardActivity.class : StoreActivity.class);
                })
                .addOnFailureListener(e -> {
                    Log.e("ROUTER", "Get token failed -> StoreActivity", e);
                    go(StoreActivity.class);
                });

        // Fallback an toàn: nếu vì lý do gì đó không callback trong ~2s, vẫn thoát Splash
        getWindow().getDecorView().postDelayed(() -> {
            if (!navigated.get()) {
                Log.w("ROUTER", "Token timeout fallback -> StoreActivity");
                go(StoreActivity.class);
            }
        }, 2000);
    }

    private void go(Class<?> target) {
        if (navigated.getAndSet(true)) return; // chặn điều hướng 2 lần
        Intent i = new Intent(this, target);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);
    }
}
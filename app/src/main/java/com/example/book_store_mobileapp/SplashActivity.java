package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.book_store_mobileapp.ui.admin.AdminDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "ROUTER";
    private static final long TIMEOUT_MS = 8000;

    private final AtomicBoolean navigated = new AtomicBoolean(false);
    private volatile boolean waiting = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        splash.setKeepOnScreenCondition(() -> waiting);
        super.onCreate(savedInstanceState);

        long start = SystemClock.elapsedRealtime();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "Current user: " + (user == null ? "null" : user.getEmail()));

        if (user == null) {
            // ✅ YÊU CẦU CỦA BẠN: KHÁCH (chưa login) -> vào thẳng StoreActivity
            Log.w(TAG, "Guest user -> StoreActivity");
            waiting = false;
            go(StoreActivity.class);
            return;
        }

        // Đã có session -> refresh token để lấy claim mới nhất
        user.getIdToken(true)
                .addOnSuccessListener((GetTokenResult result) -> {
                    Map<String, Object> claims = result.getClaims();
                    boolean isAdmin = Boolean.TRUE.equals(claims.get("admin"));
                    Log.d(TAG, "Token OK in " + (SystemClock.elapsedRealtime() - start) + "ms"
                            + ", isAdmin=" + isAdmin + ", claims=" + claims);

                    waiting = false;
                    go(isAdmin ? AdminDashboardActivity.class : StoreActivity.class);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getIdToken(true) FAILED: " + e.getMessage());
                    // Lỗi token -> vẫn cho vào Store như guest
                    waiting = false;
                    go(StoreActivity.class);
                });

        // Timeout guard: nếu quá 8s chưa xong, cho vào Store như guest
        getWindow().getDecorView().postDelayed(() -> {
            if (!navigated.get()) {
                Log.w(TAG, "Token timeout (" + TIMEOUT_MS + "ms) -> StoreActivity");
                waiting = false;
                go(StoreActivity.class);
            }
        }, TIMEOUT_MS);
    }

    private void go(Class<?> target) {
        if (navigated.getAndSet(true)) return;
        Intent i = new Intent(this, target);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);
    }
}

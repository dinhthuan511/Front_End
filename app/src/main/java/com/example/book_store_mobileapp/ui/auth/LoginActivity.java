package com.example.book_store_mobileapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.book_store_mobileapp.BaseActivity;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.StoreActivity;
import com.example.book_store_mobileapp.network.FirebaseAuthService; // ✅ import service đúng package
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Login:
 * - Hỗ trợ login bằng email hoặc username (lookup Firestore).
 * - Nếu đã đăng nhập: đọc claim 'admin' và điều hướng.
 * - Sau khi login:
 *      + Admin  -> AdminDashboardActivity
 *      + User   -> StoreActivity
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private EditText editUsernameOrEmail, editPassword;
    private Button btnLogin;
    private TextView btnGoToRegister;

    private FirebaseAuthService authService; // ✅ dùng service
    private FirebaseFirestore db;

    // Ngăn điều hướng lặp do onStart() + lấy claims
    private boolean alreadyRouted = false;

    @Override
    protected void onStart() {
        super.onStart();

        // ✅ Nếu đã có session → đọc claim và điều hướng luôn
        FirebaseUser u = (authService != null) ? authService.currentUser() : null;
        if (u != null && !alreadyRouted) {
            setUiLoading(true);
            // lấy claims (không ép refresh để nhanh)
            authService.getIdTokenClaims(false, claimsRes -> {
                if (!claimsRes.isSuccess()) {            // 🔧 dùng getter
                    setUiLoading(false);
                    toast("Không lấy được claim: " + claimsRes.getMessage()); // 🔧 dùng getter
                    return;
                }
                boolean isAdmin = Boolean.TRUE.equals(claimsRes.getData().get("admin")); // 🔧 dùng getter
                Log.d(TAG, "onStart -> isAdmin=" + isAdmin);
                alreadyRouted = true;
                routeAfterLogin(isAdmin);
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new FirebaseAuthService(); // ✅
        db         = FirebaseFirestore.getInstance();

        editUsernameOrEmail = findViewById(R.id.editUsernameOrEmail);
        editPassword        = findViewById(R.id.editPassword);
        btnLogin            = findViewById(R.id.btnLogin);
        btnGoToRegister     = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> login());

        btnGoToRegister.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
            overridePendingTransition(0, 0);
        });
    }

    private void login() {
        String id   = editUsernameOrEmail.getText().toString().trim();
        String pass = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pass)) {
            toast("Nhập username/email và mật khẩu");
            return;
        }

        setUiLoading(true);

        if (id.contains("@")) {
            // Đăng nhập bằng email
            signInWithEmail(id, pass);
        } else {
            // Đăng nhập bằng username: tra email trong Firestore
            db.collection("usernames").document(id.toLowerCase()).get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.exists()) {
                            setUiLoading(false);
                            toast("Username không tồn tại");
                            return;
                        }
                        String email = snap.getString("email");
                        if (TextUtils.isEmpty(email)) {
                            setUiLoading(false);
                            toast("Không tìm thấy email của username");
                            return;
                        }
                        signInWithEmail(email, pass);
                    })
                    .addOnFailureListener(e -> {
                        setUiLoading(false);
                        toast("Lỗi tra username: " + e.getMessage());
                    });
        }
    }

    private void signInWithEmail(String email, String pass) {
        // ✅ Gọi service thay vì gọi trực tiếp FirebaseAuth
        authService.login(email, pass, res -> {
            if (!res.isSuccess()) {                      // 🔧 dùng getter
                setUiLoading(false);
                toast("Sai thông tin đăng nhập");
                return;
            }

            // ✅ Ép refresh token để lấy custom claim 'admin' mới nhất
            authService.getIdTokenClaims(true, claimsRes -> {
                if (!claimsRes.isSuccess()) {            // 🔧 dùng getter
                    setUiLoading(false);
                    toast("Không lấy được claim: " + claimsRes.getMessage()); // 🔧
                    return;
                }
                Map<String, Object> claims = claimsRes.getData(); // 🔧
                boolean isAdmin = Boolean.TRUE.equals(claims.get("admin"));
                Log.d(TAG, "claims=" + claims);
                Log.d(TAG, "routeAfterLogin (login) isAdmin=" + isAdmin);

                toast("Đăng nhập thành công!");
                alreadyRouted = true; // tránh onStart() redirect thêm lần nữa
                routeAfterLogin(isAdmin);
            });
        });
    }

    /**
     * Điều hướng sau đăng nhập:
     * - Admin  -> AdminDashboardActivity
     * - User   -> StoreActivity
     */
    private void routeAfterLogin(boolean isAdmin) {
        Log.d(TAG, "Starting " + (isAdmin ? "AdminDashboardActivity" : "StoreActivity"));
        Intent i = new Intent(this, isAdmin ? com.example.book_store_mobileapp.ui.admin.AdminDashboardActivity.class : StoreActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void setUiLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        // Nếu có ProgressBar, bật/tắt ở đây
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_profile;
    }
}
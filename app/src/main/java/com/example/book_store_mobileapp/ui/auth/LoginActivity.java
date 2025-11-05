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
import androidx.appcompat.app.AppCompatActivity;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.StoreActivity;
import com.example.book_store_mobileapp.network.FirebaseAuthService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Login:
 * - Hỗ trợ login bằng email hoặc username (lookup Firestore).
 * - Sau khi login:
 *      + Admin  -> AdminDashboardActivity
 *      + User   -> StoreActivity
 * - ĐÃ tắt auto-route trong onStart() để tránh kẹt UI khi Play Services / mạng treo.
 *
 * Flow tổng:
 *  - App mở = StoreActivity (guest xem trước)
 *  - Nhấn Cart/Profile khi chưa login => sang LoginActivity
 *  - Đăng nhập xong:
 *      + Nếu admin => AdminDashboardActivity
 *      + Nếu user  => StoreActivity (luôn quay về Store, KHÔNG quay lại Cart/Profile)
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText editUsernameOrEmail, editPassword;
    private Button btnLogin;
    private TextView btnGoToRegister;

    private FirebaseAuthService authService;
    private FirebaseFirestore db;

    // KEEP: Cờ tránh điều hướng lặp (để dành nếu sau này bạn bật auto-route ở onStart)
    private boolean alreadyRouted = false;

    @Override
    protected void onStart() {
        super.onStart();
        // KEEP: ĐÃ TẮT auto-route để không khóa UI nếu callback không về.
        // Nếu muốn tự vào thẳng sau khi đã đăng nhập, hãy làm ở màn splash riêng.
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new FirebaseAuthService();
        db = FirebaseFirestore.getInstance();

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
        Log.d(TAG, "login(): click");
        String id   = editUsernameOrEmail.getText().toString().trim();
        String pass = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pass)) {
            toast("Nhập username/email và mật khẩu");
            return;
        }

        setUiLoading(true);

        // KEEP: DỰ PHÒNG - nếu service treo, tự bật lại nút sau 12s
        btnLogin.postDelayed(() -> {
            if (!btnLogin.isEnabled()) {
                Log.e(TAG, "login(): timeout -> re-enable button");
                setUiLoading(false);
                toast("Mạng/Play services chậm. Thử lại.");
            }
        }, 12_000);

        if (id.contains("@")) {
            // KEEP: Đăng nhập bằng email
            signInWithEmail(id, pass);
        } else {
            // KEEP: Đăng nhập bằng username -> tra email Firestore
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
        authService.login(email, pass, res -> {
            if (!res.isSuccess()) {
                setUiLoading(false);
                toast("Sai thông tin đăng nhập");
                return;
            }

            FirebaseUser u = authService.currentUser();
            if (u == null) {
                setUiLoading(false);
                toast("User null");
                return;
            }

            if (!u.isEmailVerified()) {
                try { u.sendEmailVerification(); } catch (Exception ignore) {}
                setUiLoading(false);
                toast("Email chưa xác minh. Đã gửi lại email xác minh.");

                authService.logout();
                Intent toVerify = new Intent(this, VerifyEmailActivity.class);
                toVerify.putExtra("email", u.getEmail());
                startActivity(toVerify);
                overridePendingTransition(0, 0);
                finish();
                return;
            }

            // KEEP: Ép refresh token để lấy claim 'admin' mới nhất
            authService.getIdTokenClaims(true, claimsRes -> {
                setUiLoading(false);
                if (!claimsRes.isSuccess()) {
                    toast("Không lấy được claim: " + claimsRes.getMessage());
                    return;
                }
                Map<String, Object> claims = claimsRes.getData();
                boolean isAdmin = Boolean.TRUE.equals(claims.get("admin"));

                Log.d(TAG, "claims=" + claims);
                Log.d(TAG, "routeAfterLogin (login) isAdmin=" + isAdmin);

                toast("Đăng nhập thành công!");
                alreadyRouted = true;

                // CHANGE: Luôn route theo role, KHÔNG còn quay lại Cart/Profile
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
        Intent i = new Intent(
                this,
                isAdmin
                        ? com.example.book_store_mobileapp.ui.admin.AdminDashboardActivity.class
                        : StoreActivity.class
        );
        // KEEP: Clear back stack để không quay lại Login
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
}


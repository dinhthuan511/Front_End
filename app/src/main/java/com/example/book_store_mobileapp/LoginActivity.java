package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Login:
 * - Hỗ trợ login bằng email hoặc username (lookup Firestore).
 * - Nếu đã đăng nhập: đọc claim 'admin' và điều hướng.
 * - Sau khi login:
 *      + Admin  -> AdminActivity
 *      + User   -> StoreActivity
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private EditText editUsernameOrEmail, editPassword;
    private Button btnLogin;
    private TextView btnGoToRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Ngăn điều hướng lặp do onStart() + getIdToken()
    private boolean alreadyRouted = false;

    @Override
    protected void onStart() {
        super.onStart();

        // Trường hợp người dùng đã đăng nhập mà vẫn mở LoginActivity (ví dụ từ nav):
        // → đọc claim và điều hướng ngay, tránh ở lại màn Login.
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u != null && !alreadyRouted) {
            setUiLoading(true);
            u.getIdToken(false) // chỉ đọc nhanh; không cần ép refresh ở đây
                    .addOnSuccessListener((GetTokenResult result) -> {
                        boolean isAdmin = Boolean.TRUE.equals(result.getClaims().get("admin"));
                        Log.d(TAG, "onStart -> isAdmin=" + isAdmin);
                        alreadyRouted = true; // đặt cờ trước khi điều hướng
                        routeAfterLogin(isAdmin);
                    })
                    .addOnFailureListener(e -> {
                        setUiLoading(false);
                        toast("Không lấy được claim: " + e.getMessage());
                    });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // BaseActivity nạp activity_base.xml và inflate layout con vào content_frame
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

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
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(t -> {
            if (!t.isSuccessful()) {
                setUiLoading(false);
                toast("Sai thông tin đăng nhập");
                return;
            }
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            if (u == null) {
                setUiLoading(false);
                toast("Lỗi đăng nhập");
                return;
            }

            // ÉP refresh token để lấy custom claim 'admin' mới nhất
            u.getIdToken(true).addOnSuccessListener(result -> {
                boolean isAdmin = false;
                Map<String, Object> claims = result.getClaims();
                Object v = claims.get("admin");
                if (v instanceof Boolean) isAdmin = (Boolean) v;

                Log.d(TAG, "claims=" + claims);
                Log.d(TAG, "routeAfterLogin (login) isAdmin=" + isAdmin);

                toast("Đăng nhập thành công!");
                alreadyRouted = true; // tránh onStart() redirect thêm lần nữa
                routeAfterLogin(isAdmin);
            }).addOnFailureListener(e -> {
                setUiLoading(false);
                toast("Không lấy được claim: " + e.getMessage());
            });
        });
    }

    /**
     * Điều hướng sau đăng nhập:
     * - Admin  -> AdminActivity
     * - User   -> StoreActivity
     */
    private void routeAfterLogin(boolean isAdmin) {
        Log.d(TAG, "Starting " + (isAdmin ? "AdminActivity" : "StoreActivity"));
        Intent i = new Intent(this, isAdmin ? AdminActivity.class : StoreActivity.class);
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

    // Highlight đúng tab Profile khi ở màn Login (nếu bạn dùng BottomNav)
    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_profile;
    }
}

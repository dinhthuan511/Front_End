package com.example.book_store_mobileapp.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.book_store_mobileapp.BaseActivity;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAuthService; // ✅ dùng service
import com.google.firebase.auth.FirebaseAuth; // chỉ dùng signOut cuối flow (có thể dùng service.logout nếu thích)
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private EditText edtUsername, edtEmail, edtPassword, edtPhone, edtAddress;
    private Button btnRegister;
    private TextView btnGoToLogin;

    // ✅ Dùng service + Firestore
    private FirebaseAuthService authService;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // BaseActivity sẽ nạp activity_base.xml và inflate layout con vào content_frame
        setContentView(R.layout.activity_register);

        authService = new FirebaseAuthService(); // ✅
        db         = FirebaseFirestore.getInstance();

        edtUsername  = findViewById(R.id.edtUsername);
        edtEmail     = findViewById(R.id.edtEmail);
        edtPassword  = findViewById(R.id.edtPassword);
        edtPhone     = findViewById(R.id.edtPhone);
        edtAddress   = findViewById(R.id.edtAddress);

        btnRegister  = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnRegister.setOnClickListener(v -> register());

        if (btnGoToLogin != null) {
            btnGoToLogin.setOnClickListener(v -> {
                // quay lại LoginActivity (giữ cụm Profile)
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                finish();
            });
        }
    }

    private void register() {
        final String username = edtUsername.getText().toString().trim();
        final String email    = edtEmail.getText().toString().trim();
        final String pass     = edtPassword.getText().toString().trim();
        final String phone    = edtPhone.getText().toString().trim();
        final String address  = edtAddress.getText().toString().trim();

        // ===== Validate đầu vào =====
        if (!isValidUsername(username)) {
            toast("Username 3–20 ký tự, chỉ chữ/số/._");
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Email không hợp lệ");
            return;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            toast("Mật khẩu ≥ 6 ký tự");
            return;
        }
        if (!TextUtils.isEmpty(phone) && phone.length() < 8) {
            toast("Số điện thoại không hợp lệ");
            return;
        }

        setUiLoading(true);

        // ✅ 1) Tạo tài khoản Auth qua service
        authService.register(email, pass, res -> {
            if (!res.isSuccess() || res.getData() == null) {
                setUiLoading(false);
                String msg = (res.getMessage() != null ? res.getMessage() : "Đăng ký thất bại");
                toast("Đăng ký thất bại: " + msg);
                return;
            }
            final String uid = res.getData().getUid();

            final String key = username.toLowerCase(Locale.ROOT);

            // Payload ghi vào usernames/{key}
            final Map<String, Object> usernameDoc = new HashMap<>();
            usernameDoc.put("email", email);
            usernameDoc.put("uid", uid);

            final DocumentReference unameRef = db.collection("usernames").document(key);

            // 2) Transaction: chỉ set khi doc CHƯA tồn tại
            db.runTransaction(transaction -> {
                DocumentSnapshot snap = transaction.get(unameRef);
                if (snap.exists()) {
                    throw new FirebaseFirestoreException(
                            "ALREADY_EXISTS",
                            FirebaseFirestoreException.Code.ALREADY_EXISTS
                    );
                }
                transaction.set(unameRef, usernameDoc);
                return null;
            }).addOnSuccessListener(v -> {
                // 3) Tạo hồ sơ tối thiểu users/{uid}
                Map<String, Object> profile = new HashMap<>();
                profile.put("username", username);
                profile.put("email", email);
                if (!TextUtils.isEmpty(phone))   profile.put("phone", phone);
                if (!TextUtils.isEmpty(address)) profile.put("address", address);

                db.collection("users").document(uid).set(profile)
                        .addOnCompleteListener(t -> {
                            setUiLoading(false);
                            // ✅ Sau khi đăng ký thành công, đăng xuất và quay lại Login
                            // Có thể dùng: new FirebaseAuthService().logout();
                            FirebaseAuth.getInstance().signOut();
                            toast("Đăng ký thành công! Hãy đăng nhập để tiếp tục.");
                            Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            overridePendingTransition(0, 0);
                            finish();
                        });

            }).addOnFailureListener(e -> {
                setUiLoading(false);
                // Nếu fail vì trùng username → xoá user Auth vừa tạo để tránh orphan account (tuỳ chọn)
                FirebaseAuth.getInstance().getCurrentUser(); // không xoá ở đây vì service hiện không expose delete
                FirebaseAuth.getInstance().signOut();

                if (e instanceof FirebaseFirestoreException
                        && ((FirebaseFirestoreException) e).getCode()
                        == FirebaseFirestoreException.Code.ALREADY_EXISTS) {
                    toast("Username đã được dùng, chọn tên khác");
                } else {
                    toast("Lỗi lưu username: " + (e.getMessage() != null ? e.getMessage() : ""));
                }
            });
        });
    }

    private boolean isValidUsername(String u) {
        return !(TextUtils.isEmpty(u) || u.length() < 3 || u.length() > 20)
                && u.matches("^[A-Za-z0-9._]+$");
    }

    private void setUiLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        // TODO: nếu có ProgressBar, bật/tắt ở đây
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected int getNavigationMenuItemId() {
        // Đang ở cụm Profile → highlight icon Profile
        return R.id.nav_profile;
    }
}
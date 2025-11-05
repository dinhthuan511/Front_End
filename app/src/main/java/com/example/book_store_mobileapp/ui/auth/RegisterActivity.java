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
        if (!isValidUsername(username)) { toast("Username 3–20 ký tự, chỉ chữ/số/._"); return; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Email không hợp lệ"); return; }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) { toast("Mật khẩu ≥ 6 ký tự"); return; }
        if (!TextUtils.isEmpty(phone) && phone.length() < 8) { toast("Số điện thoại không hợp lệ"); return; }

        setUiLoading(true);

        // ✅ 0) Pre-check EMAIL đã tồn tại trong Firebase Auth chưa
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(methods -> {
                    boolean emailExists = methods.getSignInMethods() != null && !methods.getSignInMethods().isEmpty();
                    if (emailExists) {
                        setUiLoading(false);
                        toast("Email đã được sử dụng. Vui lòng đăng nhập hoặc dùng Quên mật khẩu.");
                        return;
                    }

                    // ✅ 0.5) Pre-check USERNAME trùng?
                    final String key = username.toLowerCase(java.util.Locale.ROOT);
                    final DocumentReference unameRef = db.collection("usernames").document(key);

                    unameRef.get().addOnSuccessListener(snap -> {
                        if (snap.exists()) {
                            setUiLoading(false);
                            toast("Username đã được dùng, chọn tên khác");
                            return;
                        }

                        // ✅ 1) Tạo tài khoản Auth
                        authService.register(email, pass, res -> {
                            if (!res.isSuccess() || res.getData() == null) {
                                setUiLoading(false);
                                String msg = (res.getMessage() != null ? res.getMessage() : "Đăng ký thất bại");
                                toast("Đăng ký thất bại: " + msg);
                                return;
                            }

                            // ✅ 2) Gửi email xác minh NGAY
                            authService.sendVerification(vRes -> {
                                setUiLoading(false);
                                if (!vRes.isSuccess()) {
                                    toast("Không gửi được email xác minh: " + vRes.getMessage());
                                } else {
                                    toast("Đã gửi email xác minh. Vui lòng kiểm tra Hộp thư/Spam.");
                                }

                                // ✅ 3) Sang màn chờ xác minh (KHÔNG finish để form còn dữ liệu khi quay lại)
                                Intent i = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                                i.putExtra("username", username);
                                i.putExtra("email", email);
                                i.putExtra("password", pass);
                                i.putExtra("phone", phone);
                                i.putExtra("address", address);
                                startActivity(i);
                                overridePendingTransition(0, 0);
                                // không finish() để quay lại giữ nguyên form
                            });
                        });

                    }).addOnFailureListener(e -> {
                        setUiLoading(false);
                        toast("Lỗi kiểm tra username: " + (e.getMessage()!=null? e.getMessage():""));
                    });

                })
                .addOnFailureListener(e -> {
                    setUiLoading(false);
                    toast("Lỗi kiểm tra email: " + e.getMessage());
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

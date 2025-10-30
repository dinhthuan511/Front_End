package com.example.book_store_mobileapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.example.book_store_mobileapp.BaseActivity;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAuthService; // ✅ service
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountSecurityActivity extends BaseActivity {

    private EditText edtUsername, edtEmail, edtPhone, edtAddress;
    private TextView tvChangeEmail;
    private Button btnChangePassword, btnSave;

    private FirebaseAuthService authService; // ✅
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private boolean dirty = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_security);

        // Toolbar
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tài khoản & bảo mật");
        }
        bar.setNavigationOnClickListener(v -> finish());

        authService = new FirebaseAuthService(); // ✅

        // Views
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail    = findViewById(R.id.edtEmail);
        edtPhone    = findViewById(R.id.edtPhone);
        edtAddress  = findViewById(R.id.edtAddress);
        tvChangeEmail = findViewById(R.id.tvChangeEmail);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSave = findViewById(R.id.btnSave);

        loadProfile();

        TextWatcher watcher = new SimpleWatcher(() -> setDirty(true));
        edtPhone.addTextChangedListener(watcher);
        edtAddress.addTextChangedListener(watcher);

        btnChangePassword.setOnClickListener(
                v -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        tvChangeEmail.setOnClickListener(v -> showChangeEmailDialog());

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) {
                        String username = snap.getString("username");
                        String phone = snap.getString("phone");
                        String address = snap.getString("address");
                        if (!TextUtils.isEmpty(username)) edtUsername.setText(username);
                        if (!TextUtils.isEmpty(phone)) edtPhone.setText(phone);
                        if (!TextUtils.isEmpty(address)) edtAddress.setText(address);
                    }
                });
    }

    private void setDirty(boolean value) {
        dirty = value;
        btnSave.setEnabled(value);
        btnSave.setBackgroundTintList(ContextCompat.getColorStateList(
                this, value ? android.R.color.holo_red_dark : android.R.color.darker_gray));
    }

    private void saveProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (!TextUtils.isEmpty(phone) && phone.length() < 8) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("phone", TextUtils.isEmpty(phone) ? null : phone);
        updates.put("address", TextUtils.isEmpty(address) ? null : address);

        db.collection("users").document(user.getUid())
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                    setDirty(false);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ========= ĐỔI EMAIL (flow rút gọn): re-auth -> changeEmail -> sync Firestore =========
    private void showChangeEmailDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate layout dialog từ XML: res/layout/dialog_change_email.xml
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_email, null, false);
        EditText edtNewEmail = view.findViewById(R.id.edtNewEmail);                 // Email mới
        EditText edtCurrentPassword = view.findViewById(R.id.edtCurrentPassword);   // Mật khẩu hiện tại
        Button btnSendVerify = view.findViewById(R.id.btnSendVerify);               // nút chính sẽ dùng để đổi email NGAY
        Button btnIHaveVerified = view.findViewById(R.id.btnIHaveVerified);         // không dùng ở flow rút gọn

        // 🔁 Đổi nhãn cho rõ ý (không gửi mail verify)
        btnSendVerify.setText("Đổi email");
        btnIHaveVerified.setVisibility(View.GONE);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Đổi email (cần mật khẩu hiện tại)")
                .setView(view)
                .setCancelable(true)
                .create();
        dialog.show();

        // 1) Re-auth + đổi email ngay
        btnSendVerify.setOnClickListener(v -> {
            String newEmail = edtNewEmail.getText().toString().trim();
            String pwd = edtCurrentPassword.getText().toString();

            if (TextUtils.isEmpty(newEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, "Email mới không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(pwd)) {
                Toast.makeText(this, "Nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Re-auth qua service
            authService.reAuthenticate(user.getEmail(), pwd, step1 -> {
                if (!step1.isSuccess()) {
                    Toast.makeText(this, step1.getMessage() != null ? step1.getMessage() : "Xác thực lại thất bại", Toast.LENGTH_LONG).show();
                    return;
                }
                // ✅ Đổi email qua service
                authService.changeEmail(newEmail, step2 -> {
                    if (step2.isSuccess()) {
                        // Cập nhật UI & Firestore
                        edtEmail.setText(newEmail);
                        syncEmailToFirestore();
                        Toast.makeText(this, "Đổi email thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Không thể đổi email: " + step2.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
    }

    // 🆕 Đồng bộ email mới sang Firestore + collection mapping "usernames" (nếu có).
    private void syncEmailToFirestore() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) return;

        String uid = u.getUid();
        String newEmail = u.getEmail();
        if (TextUtils.isEmpty(newEmail)) return;

        // Cập nhật users/{uid}.email
        db.collection("users").document(uid)
                .update("email", newEmail);

        // Nếu có mapping username -> email, update luôn
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snap -> {
                    String username = snap.getString("username");
                    if (!TextUtils.isEmpty(username)) {
                        db.collection("usernames").document(username.toLowerCase())
                                .update("email", newEmail);
                    }
                });
    }

    @Override
    protected int getNavigationMenuItemId() {
        return -1;
    }

    private static class SimpleWatcher implements TextWatcher {
        private final Runnable onChange;
        SimpleWatcher(Runnable r) { this.onChange = r; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onChange.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}

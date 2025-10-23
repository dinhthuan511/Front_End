package com.example.book_store_mobileapp;

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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountSecurityActivity extends BaseActivity {

    private EditText edtUsername, edtEmail, edtPhone, edtAddress;
    private TextView tvChangeEmail;
    private Button btnChangePassword, btnSave;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
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

        // Views
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
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
        FirebaseUser user = auth.getCurrentUser();
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
        FirebaseUser user = auth.getCurrentUser();
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

    // ========= ĐỔI EMAIL: reauth + updateEmail (KHÔNG cần xác minh) =========
    private void showChangeEmailDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chỉ hỗ trợ khi tài khoản có provider mật khẩu
        boolean hasPasswordProvider = false;
        for (com.google.firebase.auth.UserInfo info : user.getProviderData()) {
            if (EmailAuthProvider.PROVIDER_ID.equals(info.getProviderId())) {
                hasPasswordProvider = true; break;
            }
        }
        if (!hasPasswordProvider) {
            Toast.makeText(this, "Tài khoản không dùng mật khẩu. Hãy đăng nhập lại bằng phương thức ban đầu rồi thử đổi email.", Toast.LENGTH_LONG).show();
            return;
        }

        final EditText edtNew = new EditText(this);
        edtNew.setHint("Email mới");

        final EditText edtPwd = new EditText(this);
        edtPwd.setHint("Mật khẩu hiện tại");
        edtPwd.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayoutCompat box = new LinearLayoutCompat(this);
        box.setOrientation(LinearLayoutCompat.VERTICAL);
        int p = (int) (12 * getResources().getDisplayMetrics().density);
        box.setPadding(p,p,p,p);
        box.addView(edtNew);
        box.addView(edtPwd);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Đổi email")
                .setView(box)
                .setPositiveButton("Xác nhận", (d,w) -> {
                    String newEmail = edtNew.getText().toString().trim();
                    String pwd = edtPwd.getText().toString();

                    if (TextUtils.isEmpty(newEmail) ||
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        Toast.makeText(this, "Email mới không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(pwd) || user.getEmail() == null) {
                        Toast.makeText(this, "Nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Re-auth trước khi updateEmail
                    AuthCredential cred = EmailAuthProvider.getCredential(user.getEmail(), pwd);
                    user.reauthenticate(cred)
                            .addOnSuccessListener(unused ->
                                    user.updateEmail(newEmail)
                                            .addOnSuccessListener(v -> {
                                                // Đồng bộ lại user & cập nhật UI/DB
                                                user.reload().addOnCompleteListener(t -> {
                                                    edtEmail.setText(newEmail);
                                                    db.collection("users").document(user.getUid())
                                                            .update("email", newEmail);
                                                    Toast.makeText(this, "Đổi email thành công", Toast.LENGTH_SHORT).show();
                                                });
                                            })
                                            .addOnFailureListener(this::handleEmailUpdateError)
                            )
                            .addOnFailureListener(this::handleReauthError);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleReauthError(Exception e) {
        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Mật khẩu cũ không đúng.", Toast.LENGTH_LONG).show();
            return;
        }
        if (e instanceof com.google.firebase.auth.FirebaseAuthException) {
            String code = ((com.google.firebase.auth.FirebaseAuthException) e).getErrorCode();
            if ("ERROR_REQUIRES_RECENT_LOGIN".equals(code)) {
                // Phiên cũ: bắt đăng nhập lại
                requireReLogin();
                return;
            }
        }
        Toast.makeText(this, "Xác thực lại thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void handleEmailUpdateError(Exception e) {
        if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            Toast.makeText(this, "Email này đã được dùng cho tài khoản khác.", Toast.LENGTH_LONG).show();
            return;
        }
        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Email mới không hợp lệ.", Toast.LENGTH_LONG).show();
            return;
        }
        if (e instanceof com.google.firebase.auth.FirebaseAuthException) {
            String code = ((com.google.firebase.auth.FirebaseAuthException) e).getErrorCode();
            if ("ERROR_REQUIRES_RECENT_LOGIN".equals(code)) {
                requireReLogin();
                return;
            }
        }
        Toast.makeText(this, "Không thể đổi email: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void requireReLogin() {
        Toast.makeText(this, "Phiên đăng nhập đã cũ. Vui lòng đăng nhập lại để đổi email.", Toast.LENGTH_LONG).show();
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_profile;
    }

    private static class SimpleWatcher implements TextWatcher {
        private final Runnable onChange;
        SimpleWatcher(Runnable r) { this.onChange = r; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onChange.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}

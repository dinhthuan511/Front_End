package com.example.book_store_mobileapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends BaseActivity {

    private EditText edtOldPassword, edtNewPassword, edtConfirmPassword;
    private Button btnSubmit;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // ===== Toolbar: xử lý nút mũi tên quay lại =====
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đổi mật khẩu");
        }
        bar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        // =================================================

        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSubmit = findViewById(R.id.btnConfirmChange);

        btnSubmit.setOnClickListener(v -> doChangePassword());
    }

    private void doChangePassword() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || TextUtils.isEmpty(user.getEmail())) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String oldPwd = edtOldPassword.getText().toString();
        String newPwd = edtNewPassword.getText().toString();
        String confirm = edtConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPwd.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải ≥ 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPwd.equals(confirm)) {
            Toast.makeText(this, "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential cred = EmailAuthProvider.getCredential(user.getEmail(), oldPwd);
        user.reauthenticate(cred).addOnSuccessListener(unused ->
                user.updatePassword(newPwd).addOnSuccessListener(u -> {
                    Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    finish(); // quay lại trang AccountSecurityActivity
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi đổi mật khẩu: " + e.getMessage(), Toast.LENGTH_SHORT).show())
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_profile;
    }
}

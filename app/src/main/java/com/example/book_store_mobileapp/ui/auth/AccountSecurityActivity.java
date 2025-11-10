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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.book_store_mobileapp.BaseActivity;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAuthService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountSecurityActivity extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtPhone, edtAddress;
    private Button btnChangePassword, btnSave;

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
        edtEmail    = findViewById(R.id.edtEmail);
        edtPhone    = findViewById(R.id.edtPhone);
        edtAddress  = findViewById(R.id.edtAddress);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSave = findViewById(R.id.btnSave);

        loadProfile();

        TextWatcher watcher = new SimpleWatcher(() -> setDirty(true));
        edtPhone.addTextChangedListener(watcher);
        edtAddress.addTextChangedListener(watcher);

        btnChangePassword.setOnClickListener(
                v -> startActivity(new Intent(this, ChangePasswordActivity.class))
        );

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
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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



    private static class SimpleWatcher implements TextWatcher {
        private final Runnable onChange;
        SimpleWatcher(Runnable r) { this.onChange = r; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onChange.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
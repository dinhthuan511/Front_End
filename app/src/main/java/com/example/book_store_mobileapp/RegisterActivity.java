package com.example.book_store_mobileapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtPassword;
    private Button btnRegister;
    private TextView btnGoToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        edtUsername  = findViewById(R.id.edtUsername);
        edtEmail     = findViewById(R.id.edtEmail);
        edtPassword  = findViewById(R.id.edtPassword);
        btnRegister  = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnRegister.setOnClickListener(v -> register());

        if (btnGoToLogin != null) {
            btnGoToLogin.setOnClickListener(v -> finish());
        }
    }

    private void register() {
        final String username = edtUsername.getText().toString().trim();
        final String email    = edtEmail.getText().toString().trim();
        final String pass     = edtPassword.getText().toString().trim();

        if (!isValidUsername(username)) { toast("Username 3–20 ký tự, chỉ chữ/số/._"); return; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Email không hợp lệ"); return; }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) { toast("Mật khẩu ≥ 6 ký tự"); return; }

        setUiLoading(true);

        // 1) Tạo tài khoản Auth (sau bước này user đã đăng nhập)
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser cur = FirebaseAuth.getInstance().getCurrentUser();
                    if (cur == null) {
                        setUiLoading(false);
                        toast("Lỗi: không tìm thấy phiên đăng nhập sau khi tạo tài khoản");
                        return;
                    }

                    final String uid = cur.getUid();
                    final String key = username.toLowerCase(Locale.ROOT);

                    // Payload ghi vào usernames/{key}
                    final Map<String, Object> usernameDoc = new HashMap<>();
                    usernameDoc.put("email", email);
                    usernameDoc.put("uid", uid);

                    final DocumentReference unameRef = db.collection("usernames").document(key);

                    // 2) Transaction: chỉ set khi doc CHƯA tồn tại (create-only semantics)
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
                        // 3) (Tuỳ chọn) tạo hồ sơ tối thiểu users/{uid}
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("username", username);
                        profile.put("email", email);

                        db.collection("users").document(uid).set(profile)
                                .addOnCompleteListener(t -> {
                                    setUiLoading(false);

                                    // 🔸 Thêm 2 dòng này để đăng xuất và quay về LoginActivity
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    toast("Đăng ký thành công! Hãy đăng nhập bằng USERNAME + mật khẩu");
                                    finish();

                                });


                    }).addOnFailureListener(e -> {
                        setUiLoading(false);
                        if (e instanceof FirebaseFirestoreException
                                && ((FirebaseFirestoreException) e).getCode()
                                == FirebaseFirestoreException.Code.ALREADY_EXISTS) {
                            toast("Username đã được dùng, chọn tên khác");
                        } else {
                            toast("Lỗi lưu username: " + (e.getMessage() != null ? e.getMessage() : ""));
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    setUiLoading(false);
                    String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Đăng ký thất bại";
                    // Gợi ý nguyên nhân phổ biến cho dev
                    if (msg.contains("email address is already in use")) {
                        toast("Email đã đăng ký, thử email khác");
                    } else if (msg.contains("badly formatted")) {
                        toast("Email không hợp lệ");
                    } else {
                        toast("Đăng ký thất bại: " + msg);
                    }
                });
    }

    private boolean isValidUsername(String u) {
        return !(TextUtils.isEmpty(u) || u.length() < 3 || u.length() > 20)
                && u.matches("^[A-Za-z0-9._]+$");
    }

    private void setUiLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        // Có thể thêm ProgressBar nếu muốn
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}

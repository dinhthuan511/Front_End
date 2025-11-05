package com.example.book_store_mobileapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAuthService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VerifyEmailActivity extends AppCompatActivity {

    private FirebaseAuthService authService;
    private FirebaseFirestore db;

    private String username, email, password, phone, address;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        authService = new FirebaseAuthService();
        db = FirebaseFirestore.getInstance();

        // Nhận dữ liệu từ RegisterActivity (để hoàn tất profile sau verify)
        Intent i = getIntent();
        username = i.getStringExtra("username");
        email    = i.getStringExtra("email");
        password = i.getStringExtra("password");
        phone    = i.getStringExtra("phone");
        address  = i.getStringExtra("address");

        TextView tvHint = findViewById(R.id.tvHint);
        if (email != null) {
            tvHint.setText("Đã gửi email xác minh tới:\n" + email + "\nVui lòng mở email và bấm vào liên kết xác minh.");
        }

        Button btnResend         = findViewById(R.id.btnResend);
        Button btnVerified       = findViewById(R.id.btnVerified);
        Button btnBackToRegister = findViewById(R.id.btnBackToRegister);

        // Gửi lại email xác minh
        btnResend.setOnClickListener(v -> resend());

        // “Tôi đã xác minh”: kiểm tra thật bằng reload từ server
        btnVerified.setOnClickListener(v -> confirmVerifiedAndFinish());

        // “Quay lại đăng ký”: nếu CHƯA verify thì xoá user tạm để email không bị chiếm
        btnBackToRegister.setOnClickListener(v -> backOrCancelRegistration());

        // Bắt nút back hệ thống cho hành vi giống nút “Quay lại đăng ký”
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { backOrCancelRegistration(); }
        });
    }

    private void resend() {
        authService.sendVerification(res -> {
            if (!res.isSuccess()) toast("Gửi lại thất bại: " + res.getMessage());
            else toast("Đã gửi lại email xác minh.");
        });
    }

    private void confirmVerifiedAndFinish() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) { toast("Chưa đăng nhập"); return; }

        u.reload().addOnSuccessListener(v -> {
            if (!u.isEmailVerified()) {
                toast("Email chưa được xác minh. Vui lòng bấm link trong email rồi thử lại.");
                return;
            }
            // ĐÃ XÁC MINH → hoàn tất đăng ký: ghi Firestore
            finishRegistrationAfterVerified(u);

        }).addOnFailureListener(e -> toast("Lỗi kiểm tra trạng thái: " + e.getMessage()));
    }

    /** Quay lại đăng ký; nếu chưa verify thì hủy account tạm để giải phóng email */
    private void backOrCancelRegistration() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) { finish(); return; }

        u.reload().addOnSuccessListener(ignored -> {
            if (!u.isEmailVerified()) {
                u.delete().addOnCompleteListener(task -> {
                    FirebaseAuth.getInstance().signOut();
                    toast("Đã huỷ đăng ký và giải phóng email.");
                    finish(); // quay lại Register (form còn nguyên vì Register chưa finish)
                });
            } else {
                finish(); // đã verify thì không xoá
            }
        }).addOnFailureListener(e -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
    }

    private void finishRegistrationAfterVerified(FirebaseUser u) {
        final String uid = u.getUid();
        final String key = username == null ? "" : username.toLowerCase(Locale.ROOT);

        final Map<String, Object> usernameDoc = new HashMap<>();
        usernameDoc.put("email", email);
        usernameDoc.put("uid", uid);

        final DocumentReference unameRef = db.collection("usernames").document(key);

        // Transaction: chỉ set khi CHƯA tồn tại
        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(unameRef);
            if (snap.exists()) {
                throw new FirebaseFirestoreException("ALREADY_EXISTS",
                        FirebaseFirestoreException.Code.ALREADY_EXISTS);
            }
            transaction.set(unameRef, usernameDoc);
            return null;
        }).addOnSuccessListener(v -> {
            Map<String, Object> profile = new HashMap<>();
            profile.put("username", username);
            profile.put("email", email);
            if (!TextUtils.isEmpty(phone))   profile.put("phone", phone);
            if (!TextUtils.isEmpty(address)) profile.put("address", address);

            db.collection("users").document(uid).set(profile)
                    .addOnSuccessListener(ignored -> {
                        toast("Xác minh thành công! Đăng ký hoàn tất.");
                        // Đưa về Login để user đăng nhập bình thường
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> toast("Lỗi lưu user: " + e.getMessage()));
        }).addOnFailureListener(e -> {
            // Nếu username đã bị dùng trong lúc bạn đi xác minh, báo lại
            toast("Username đã được sử dụng. Vui lòng chọn tên khác.");
        });
    }

    private void toast(String m){ Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}

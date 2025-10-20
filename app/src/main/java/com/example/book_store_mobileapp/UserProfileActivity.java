package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvUsername, tvUid;
    private EditText edtEmail, edtPhone, edtAddress;
    private Button btnEdit, btnSave, btnCancel, btnChangePass, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        FirebaseUser cur = mAuth.getCurrentUser();
        if (cur == null) {
            // chưa đăng nhập → về Login
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        userRef = db.collection("users").document(cur.getUid());

        // bind view
        imgAvatar   = findViewById(R.id.imgAvatar);
        tvUsername  = findViewById(R.id.tvUsername);
        tvUid       = findViewById(R.id.tvUid);
        edtEmail    = findViewById(R.id.edtEmail);
        edtPhone    = findViewById(R.id.edtPhone);
        edtAddress  = findViewById(R.id.edtAddress);
        btnEdit     = findViewById(R.id.btnEdit);
        btnSave     = findViewById(R.id.btnSave);
        btnCancel   = findViewById(R.id.btnCancel);
        btnChangePass = findViewById(R.id.btnChangePass);
        btnLogout   = findViewById(R.id.btnLogout);

        // header
        tvUid.setText("UID: " + cur.getUid());
        // imgAvatar: dùng placeholder mặc định; bạn có thể tích hợp Glide/Picasso sau

        setEditMode(false);
        loadProfile();

        btnEdit.setOnClickListener(v -> setEditMode(true));

        btnCancel.setOnClickListener(v -> {
            setEditMode(false);
            loadProfile(); // tải lại để hủy thay đổi
        });

        btnSave.setOnClickListener(v -> saveProfile());

        btnChangePass.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                toast("Email trống, không thể gửi liên kết đổi mật khẩu");
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(ok -> toast("Đã gửi email đổi mật khẩu"))
                    .addOnFailureListener(e -> toast("Lỗi: " + e.getMessage()));
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }

    private void loadProfile() {
        // mặc định show email từ Auth nếu Firestore chưa có
        FirebaseUser cur = mAuth.getCurrentUser();
        String fallbackEmail = (cur != null && cur.getEmail() != null) ? cur.getEmail() : "";

        userRef.get().addOnSuccessListener(snap -> {
            String username = snap.getString("username");
            String email    = snap.getString("email");
            String phone    = snap.getString("phone");
            String address  = snap.getString("address");

            tvUsername.setText(!TextUtils.isEmpty(username) ? username : "Người dùng");
            edtEmail.setText(!TextUtils.isEmpty(email) ? email : fallbackEmail);
            edtPhone.setText(phone != null ? phone : "");
            edtAddress.setText(address != null ? address : "");
        }).addOnFailureListener(e -> toast("Không tải được hồ sơ: " + e.getMessage()));
    }

    private void saveProfile() {
        String email   = edtEmail.getText().toString().trim();
        String phone   = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        Map<String, Object> payload = new HashMap<>();
        if (!TextUtils.isEmpty(email))   payload.put("email", email);
        payload.put("phone",   phone);
        payload.put("address", address);

        userRef.set(payload, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(v -> {
                    toast("Đã lưu hồ sơ");
                    setEditMode(false);
                })
                .addOnFailureListener(e -> toast("Lưu thất bại: " + e.getMessage()));
    }

    private void setEditMode(boolean enable) {
        editMode = enable;
        edtPhone.setEnabled(enable);
        edtAddress.setEnabled(enable);

        // Email thường để không sửa ở app (đổi email ảnh hưởng Auth). Nếu muốn sửa, đặt enabled = true và dùng re-auth/verify.
        edtEmail.setEnabled(false);

        btnEdit.setVisibility(enable ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}

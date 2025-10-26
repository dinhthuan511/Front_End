package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

// 🆕 import cho dialog dùng layout XML
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

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

    // ========= ĐỔI EMAIL: reauth + verifyBeforeUpdateEmail (CẦN xác minh) =========
// Flow:
// 1) Nhập email mới + mật khẩu hiện tại -> re-auth
// 2) Gửi email xác minh tới email mới (verifyBeforeUpdateEmail)
// 3) User bấm link trong email
// 4) Quay lại app -> bấm "Tôi đã xác minh xong" -> reload() -> nếu email đổi => sync Firestore
// 5) Nếu chưa đổi (không xác minh / link hết hạn) -> Cho phép GỬI LẠI email xác minh (có re-auth lại)
    private void showChangeEmailDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chỉ hỗ trợ khi tài khoản có provider mật khẩu (để re-auth bằng password).
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

        // Inflate layout dialog từ XML: res/layout/dialog_change_email.xml
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_email, null, false);
        EditText edtNewEmail = view.findViewById(R.id.edtNewEmail);                 // Email mới
        EditText edtCurrentPassword = view.findViewById(R.id.edtCurrentPassword);   // Mật khẩu hiện tại (để re-auth)
        Button btnSendVerify = view.findViewById(R.id.btnSendVerify);               // Gửi email xác minh
        Button btnIHaveVerified = view.findViewById(R.id.btnIHaveVerified);         // Tôi đã xác minh xong

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Đổi email (cần xác minh)")
                .setView(view)
                .setCancelable(true)
                .create();
        dialog.show();

        // 1) GỬI MAIL XÁC MINH TỚI EMAIL MỚI
        btnSendVerify.setOnClickListener(v -> {
            FirebaseUser u = auth.getCurrentUser();
            if (u == null) { Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show(); return; }

            String newEmail = edtNewEmail.getText().toString().trim();
            String pwd = edtCurrentPassword.getText().toString();

            if (TextUtils.isEmpty(newEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, "Email mới không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(pwd) || u.getEmail() == null) {
                Toast.makeText(this, "Nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }

            // Set language cho email template (tuỳ chọn)
            auth.setLanguageCode("vi");

            Log.d("EMAIL_DEBUG", "Start RE-AUTH for email change. currentEmail=" + u.getEmail() + ", newEmail=" + newEmail);
            // Re-auth trước khi verifyBeforeUpdateEmail (bắt buộc theo yêu cầu bảo mật Firebase)
            AuthCredential cred = EmailAuthProvider.getCredential(u.getEmail(), pwd);
            u.reauthenticate(cred).addOnSuccessListener(unused -> {
                Log.d("EMAIL_DEBUG", "Re-authentication SUCCESS. Sending verifyBeforeUpdateEmail to " + newEmail);

                u.verifyBeforeUpdateEmail(newEmail)
                        .addOnSuccessListener(ignored -> {
                            Log.d("EMAIL_DEBUG", "verifyBeforeUpdateEmail SENT to " + newEmail);
                            Toast.makeText(this, "Đã gửi email xác minh tới: " + newEmail + ". Vui lòng mở mail và xác minh.", Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("EMAIL_DEBUG", "verifyBeforeUpdateEmail FAILED", e);
                            handleEmailUpdateError(e);
                        });

            }).addOnFailureListener(e -> {
                Log.e("EMAIL_DEBUG", "Re-authentication FAILED", e);
                handleReauthError(e);
            });
        });

        // 2) SAU KHI USER ĐÃ BẤM LINK XÁC MINH TRONG MAIL
        btnIHaveVerified.setOnClickListener(v -> {
            FirebaseUser u = auth.getCurrentUser();
            if (u == null) { Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show(); return; }

            final String oldEmail = u.getEmail(); // Lưu email cũ để so sánh sau reload()
            Log.d("EMAIL_DEBUG", "Reloading user to check verification… oldEmail=" + oldEmail);

            u.reload().addOnSuccessListener(x -> {
                String newEmailAfter = u.getEmail();
                Log.d("EMAIL_DEBUG", "Reload DONE. currentUser.getEmail()=" + newEmailAfter);

                // Nếu khác oldEmail => đã đổi thành công
                if (newEmailAfter != null && !newEmailAfter.equalsIgnoreCase(oldEmail)) {
                    Log.d("EMAIL_DEBUG", "Email CHANGED after verification. Updating UI & Firestore…");
                    edtEmail.setText(newEmailAfter);   // Cập nhật UI
                    syncEmailToFirestore();            // Đồng bộ Firestore + mapping username (nếu có)
                    Toast.makeText(this, "Đã cập nhật email sau xác minh", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Log.w("EMAIL_DEBUG", "Email NOT changed. Probably link not clicked/expired. Offering RESEND…");
                    // ❌ Chưa đổi được (chưa bấm link / link lỗi / link hết hạn)
                    new AlertDialog.Builder(this)
                            .setTitle("Chưa xác minh được email")
                            .setMessage("Có thể bạn chưa bấm link trong email, hoặc link đã hết hạn.\nBạn muốn gửi lại email xác minh không?")
                            .setPositiveButton("Gửi lại", (d, w) -> {
                                String newEmail = edtNewEmail.getText().toString().trim();
                                String pwd = edtCurrentPassword.getText().toString();

                                if (TextUtils.isEmpty(newEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                                    Toast.makeText(this, "Email mới không hợp lệ", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (TextUtils.isEmpty(pwd) || oldEmail == null) {
                                    Toast.makeText(this, "Nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Log.d("EMAIL_DEBUG", "RESEND verifyBeforeUpdateEmail to " + newEmail);
                                // Re-auth lại rồi gửi verify lại
                                AuthCredential cred = EmailAuthProvider.getCredential(oldEmail, pwd);
                                u.reauthenticate(cred).addOnSuccessListener(unused -> {
                                    Log.d("EMAIL_DEBUG", "Re-auth SUCCESS (resend).");
                                    u.verifyBeforeUpdateEmail(newEmail)
                                            .addOnSuccessListener(ignored -> {
                                                Log.d("EMAIL_DEBUG", "RESEND verifyBeforeUpdateEmail SENT to " + newEmail);
                                                Toast.makeText(this, "Đã gửi lại email xác minh", Toast.LENGTH_LONG).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("EMAIL_DEBUG", "RESEND verifyBeforeUpdateEmail FAILED", e);
                                                handleEmailUpdateError(e);
                                            });
                                }).addOnFailureListener(e -> {
                                    Log.e("EMAIL_DEBUG", "Re-auth FAILED (resend)", e);
                                    handleReauthError(e);
                                });
                            })
                            .setNegativeButton("Đóng", null)
                            .show();
                }
            }).addOnFailureListener(e -> {
                Log.e("EMAIL_DEBUG", "reload() FAILED", e);
                Toast.makeText(this, "Không thể reload tài khoản: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        });
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

    // 🆕 Đồng bộ email mới sang Firestore + collection mapping "usernames" (nếu bạn có).
    // Gọi sau khi verify xong và reload() thấy email đã đổi thành công.
    private void syncEmailToFirestore() {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) return;

        String uid = u.getUid();
        String newEmail = u.getEmail();
        if (TextUtils.isEmpty(newEmail)) return;

        // Cập nhật users/{uid}.email
        db.collection("users").document(uid)
                .update("email", newEmail)
                .addOnSuccessListener(v -> {
                    // Có thể log/tooltip nếu muốn
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi cập nhật Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

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

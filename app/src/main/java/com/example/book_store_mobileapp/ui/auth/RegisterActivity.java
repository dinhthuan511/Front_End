package com.example.book_store_mobileapp.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAuthService; // ✅ dùng service
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtPassword, edtPhone, edtAddress;
    private Button btnRegister;
    private TextView btnGoToLogin;

    // ✅ Dùng service + Firestore
    private FirebaseAuthService authService;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = new FirebaseAuthService();
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

        // Username
        if (!isValidUsername(username)) {
            toast("Username 3–20 ký tự, chỉ chữ/số/._, không có khoảng trống");
            return;
        }

        // ✅ Email: regex riêng, bắt buộc có domain .com/.vn/... chuẩn
        if (!isValidEmail(email)) {
            toast("Email không hợp lệ. Vui lòng nhập đúng định dạng (ví dụ: abc@gmail.com).");
            return;
        }

        // ✅ Password: chữ cái đầu in hoa + có số + có ký tự đặc biệt + tối thiểu 6 ký tự
        if (!isValidPassword(pass)) {
            toast("Mật khẩu phải bắt đầu bằng chữ hoa, chứa ít nhất 1 số và 1 ký tự đặc biệt (ví dụ: @, #, $, %).");
            return;
        }

        // ✅ SĐT: optional nhưng nếu nhập thì phải đúng 10 số bắt đầu bằng 0
        if (!TextUtils.isEmpty(phone) && !isValidPhone(phone)) {
            toast("Số điện thoại không hợp lệ. Vui lòng nhập 10 số, bắt đầu bằng 0.");
            return;
        }

        // ✅ Địa chỉ: optional nhưng nếu nhập phải giống địa chỉ giao hàng cơ bản
        if (!TextUtils.isEmpty(address) && !isValidAddress(address)) {
            toast("Địa chỉ không hợp lệ. Vui lòng nhập dạng: Số nhà + đường + quận/huyện/thành phố.");
            return;
        }

        setUiLoading(true);

        // ✅ 0) Pre-check EMAIL đã tồn tại trong Firebase Auth chưa
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(methods -> {
                    boolean emailExists = methods.getSignInMethods() != null
                            && !methods.getSignInMethods().isEmpty();

                    if (emailExists) {
                        // Email đã có trong Auth → xử lý case đăng ký dở / đã verify
                        handleExistingEmailFlow(username, email, pass, phone, address);
                    } else {
                        // Email chưa tồn tại → đăng ký mới
                        createNewAccount(username, email, pass, phone, address);
                    }
                })
                .addOnFailureListener(e -> {
                    setUiLoading(false);
                    toast("Lỗi kiểm tra email: " + e.getMessage());
                });
    }

    // ================== FLOW: EMAIL CHƯA TỒN TẠI ==================

    private void createNewAccount(String username, String email, String pass,
                                  String phone, String address) {

        final String key = username.toLowerCase(Locale.ROOT);
        final DocumentReference unameRef = db.collection("usernames").document(key);

        // Check username trùng
        unameRef.get().addOnSuccessListener(snap -> {
            if (snap.exists()) {
                setUiLoading(false);
                toast("Username đã được dùng, chọn tên khác");
                return;
            }

            // 1) Tạo tài khoản Auth
            authService.register(email, pass, res -> {
                if (!res.isSuccess() || res.getData() == null) {
                    setUiLoading(false);
                    String msg = (res.getMessage() != null ? res.getMessage() : "Đăng ký thất bại");
                    toast("Đăng ký thất bại: " + msg);
                    return;
                }

                // 2) Gửi email xác minh
                authService.sendVerification(vRes -> {
                    setUiLoading(false);
                    if (!vRes.isSuccess()) {
                        toast("Không gửi được email xác minh: " + vRes.getMessage());
                    } else {
                        toast("Đã gửi email xác minh. Vui lòng kiểm tra Hộp thư/Spam.");
                    }

                    // 3) Sang màn chờ xác minh
                    Intent i = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                    i.putExtra("username", username);
                    i.putExtra("email", email);
                    i.putExtra("phone", phone);
                    i.putExtra("address", address);
                    startActivity(i);
                    overridePendingTransition(0, 0);
                });
            });

        }).addOnFailureListener(e -> {
            setUiLoading(false);
            toast("Lỗi kiểm tra username: " + (e.getMessage() != null ? e.getMessage() : ""));
        });
    }

    // ================== FLOW: EMAIL ĐÃ TỒN TẠI ==================

    private void handleExistingEmailFlow(String username, String email, String pass,
                                         String phone, String address) {

        // Thử đăng nhập bằng email + pass đang nhập
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        setUiLoading(false);
                        toast("Email đã được sử dụng. Vui lòng dùng Đăng nhập hoặc Quên mật khẩu.");
                        return;
                    }

                    if (!user.isEmailVerified()) {
                        // Đã có account nhưng chưa verify
                        setUiLoading(false);
                        toast("Email này đã đăng ký nhưng chưa xác minh. Vui lòng kiểm tra email.");
                        Intent i = new Intent(this, VerifyEmailActivity.class);
                        i.putExtra("email", email);
                        startActivity(i);
                        overridePendingTransition(0, 0);
                        return;
                    }

                    // ĐÃ verify → kiểm tra profile Firestore
                    String uid = user.getUid();
                    DocumentReference userRef = db.collection("users").document(uid);

                    userRef.get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            // Đã có profile -> xem như login xong
                            setUiLoading(false);
                            toast("Tài khoản đã tồn tại. Vui lòng đăng nhập.");
                            // Có thể điều hướng sang LoginActivity nếu muốn:
                            // startActivity(new Intent(this, LoginActivity.class));
                            // finish();
                        } else {
                            // CASE PARTIAL: có Auth + verified nhưng chưa có profile
                            finalizeProfileForExistingUser(uid, username, email, phone, address);
                        }
                    }).addOnFailureListener(e -> failFinalize(e.getMessage()));

                })
                .addOnFailureListener(e -> {
                    // Sai mật khẩu / không login được
                    setUiLoading(false);
                    toast("Email đã được sử dụng. Vui lòng dùng Đăng nhập hoặc Quên mật khẩu.");
                });
    }

    // Hoàn tất tạo username + profile cho user đã verify sẵn
    private void finalizeProfileForExistingUser(String uid, String username, String email,
                                                String phone, String address) {

        final String key = username.toLowerCase(Locale.ROOT);
        DocumentReference unameRef = db.collection("usernames").document(key);
        DocumentReference userRef  = db.collection("users").document(uid);

        unameRef.get().addOnSuccessListener(unameSnap -> {
            if (unameSnap.exists()) {
                setUiLoading(false);
                toast("Username đã được dùng, chọn tên khác");
            } else {
                Map<String, Object> unameMap = new HashMap<>();
                unameMap.put("uid", uid);

                Map<String, Object> userData = new HashMap<>();
                userData.put("username", username);
                userData.put("email", email);
                if (!TextUtils.isEmpty(phone))   userData.put("phone", phone);
                if (!TextUtils.isEmpty(address)) userData.put("address", address);

                unameRef.set(unameMap)
                        .addOnSuccessListener(unused1 ->
                                userRef.set(userData)
                                        .addOnSuccessListener(unused2 -> {
                                            setUiLoading(false);
                                            toast("Hoàn tất đăng ký cho tài khoản đã xác minh.");
                                            // TODO: điều hướng sang StoreActivity/Home nếu muốn
                                            // startActivity(new Intent(this, StoreActivity.class));
                                            // finish();
                                        })
                                        .addOnFailureListener(e ->
                                                failFinalize(e.getMessage())))
                        .addOnFailureListener(e ->
                                failFinalize(e.getMessage()));
            }
        }).addOnFailureListener(e ->
                failFinalize(e.getMessage()));
    }

    private void failFinalize(String msg) {
        setUiLoading(false);
        toast("Lỗi hoàn tất đăng ký: " + (msg != null ? msg : ""));
    }

    // ================== VALIDATION HELPERS ==================

    // Username: 3–20 ký tự, chỉ a-zA-Z0-9 . _
    private boolean isValidUsername(String u) {
        return !(TextUtils.isEmpty(u) || u.length() < 3 || u.length() > 20)
                && u.matches("^[A-Za-z0-9._]+$");
    }

    // Email: phải có dạng tên@domain.tld, tld 2–6 ký tự (chặn @gmail.co)
    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        // Dạng tổng quát: tên@domain.tld (tld dài 2-6 ký tự)
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email.matches(regex);
    }

    // Password: min 6, ký tự đầu in hoa, có số, có ký tự đặc biệt
    private boolean isValidPassword(String pass) {
        if (TextUtils.isEmpty(pass) || pass.length() < 6) return false;
        if (!Character.isUpperCase(pass.charAt(0))) return false;          // chữ đầu in hoa
        if (!pass.matches(".*\\d.*")) return false;                        // có số
        if (!pass.matches(".*[^A-Za-z0-9].*")) return false;              // có ký tự đặc biệt
        return true;
    }


    private boolean isValidPhone(String p) {
        String digits = p.replaceAll("\\s+", "");
        return digits.matches("^0\\d{9}$");
    }
    
    private boolean isValidAddress(String a) {
        if (a == null) return false;
        String trimmed = a.trim();
        if (trimmed.length() < 10) return false;
        if (!trimmed.matches(".*[A-Za-zÀ-ỹ].*")) return false; // có chữ
        if (!trimmed.matches(".*\\d.*")) return false;         // có số
        if (!trimmed.contains(" ")) return false;              // có khoảng trắng
        return true;
    }

    private void setUiLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

}

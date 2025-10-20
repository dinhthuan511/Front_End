//package com.example.book_store_mobileapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.util.Log;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.Map;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private EditText editUsernameOrEmail, editPassword;
//    private Button btnLogin;
//    private TextView btnGoToRegister;
//    private FirebaseAuth mAuth;
//    private FirebaseFirestore db;
//
//    // Chặn gọi startActivity nhiều lần do onStart() / idToken callback lặp
//    private boolean alreadyRouted = false;
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
//        if (u != null && !alreadyRouted) {
//            setUiLoading(true);
//            u.getIdToken(true).addOnSuccessListener(result -> {
//                boolean isAdmin = false;
//                Map<String, Object> claims = result.getClaims();
//                Object v = claims.get("admin");
//                if (v instanceof Boolean) isAdmin = (Boolean) v;
//
//                Log.d("CLAIMS", "claims=" + claims);
//                Log.d("NAV", "routeAfterLogin (onStart) isAdmin=" + isAdmin);
//
//                alreadyRouted = true; // set cờ TRƯỚC khi điều hướng
//                routeAfterLogin(isAdmin);
//            }).addOnFailureListener(e -> {
//                setUiLoading(false);
//                toast("Không lấy được claim: " + e.getMessage());
//            });
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        mAuth = FirebaseAuth.getInstance();
//        db   = FirebaseFirestore.getInstance();
//
//        editUsernameOrEmail = findViewById(R.id.editUsernameOrEmail);
//        editPassword        = findViewById(R.id.editPassword);
//        btnLogin            = findViewById(R.id.btnLogin);
//        btnGoToRegister     = findViewById(R.id.btnGoToRegister);
//
//        btnLogin.setOnClickListener(v -> login());
//
//        btnGoToRegister.setOnClickListener(v ->
//                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
//    }
//
//    private void login() {
//        String id   = editUsernameOrEmail.getText().toString().trim();
//        String pass = editPassword.getText().toString().trim();
//
//        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pass)) {
//            toast("Nhập username/email và mật khẩu");
//            return;
//        }
//
//        setUiLoading(true);
//
//        if (id.contains("@")) {
//            // Đăng nhập bằng email
//            signInWithEmail(id, pass);
//        } else {
//            // Đăng nhập bằng username: tra email trong Firestore
//            db.collection("usernames").document(id.toLowerCase()).get()
//                    .addOnSuccessListener(snap -> {
//                        if (!snap.exists()) {
//                            setUiLoading(false);
//                            toast("Username không tồn tại");
//                            return;
//                        }
//                        String email = snap.getString("email");
//                        if (TextUtils.isEmpty(email)) {
//                            setUiLoading(false);
//                            toast("Không tìm thấy email của username");
//                            return;
//                        }
//                        signInWithEmail(email, pass);
//                    })
//                    .addOnFailureListener(e -> {
//                        setUiLoading(false);
//                        toast("Lỗi tra username: " + e.getMessage());
//                    });
//        }
//    }
//
//    private void signInWithEmail(String email, String pass) {
//        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(t -> {
//            if (!t.isSuccessful()) {
//                setUiLoading(false);
//                toast("Sai thông tin đăng nhập");
//                return;
//            }
//            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
//            if (u == null) {
//                setUiLoading(false);
//                toast("Lỗi đăng nhập");
//                return;
//            }
//
//            // Lấy custom claim 'admin' rồi điều hướng
//            u.getIdToken(true).addOnSuccessListener(result -> {
//                boolean isAdmin = false;
//                Map<String, Object> claims = result.getClaims();
//                Object v = claims.get("admin");
//                if (v instanceof Boolean) isAdmin = (Boolean) v;
//
//                Log.d("CLAIMS", "claims=" + claims);
//                Log.d("NAV", "routeAfterLogin (login) isAdmin=" + isAdmin);
//
//                toast("Đăng nhập thành công!");
//                alreadyRouted = true; // tránh onStart() redirect thêm lần nữa
//                routeAfterLogin(isAdmin);
//            }).addOnFailureListener(e -> {
//                setUiLoading(false);
//                toast("Không lấy được claim: " + e.getMessage());
//            });
//        });
//    }
//
//    private void routeAfterLogin(boolean isAdmin) {
//        Log.d("NAV", "Starting " + (isAdmin ? "AdminActivity" : "StoreActivity"));
//        Intent i = new Intent(this, isAdmin ? AdminActivity.class : StoreActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(i);
//        finish();
//    }
//
//    private void setUiLoading(boolean loading) {
//        btnLogin.setEnabled(!loading);
//        // Có thể hiển thị ProgressBar nếu muốn
//    }
//
//    private void toast(String m) {
//        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
//    }
//}


package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsernameOrEmail, editPassword;
    private Button btnLogin;
    private TextView btnGoToRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Ngăn điều hướng lặp do onStart() + getIdToken()
    private boolean alreadyRouted = false;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u != null && !alreadyRouted) {
            setUiLoading(true);
            u.getIdToken(true).addOnSuccessListener(result -> {
                boolean isAdmin = false;
                Map<String, Object> claims = result.getClaims();
                Object v = claims.get("admin");
                if (v instanceof Boolean) isAdmin = (Boolean) v;

                Log.d("CLAIMS", "claims=" + claims);
                Log.d("NAV", "routeAfterLogin (onStart) isAdmin=" + isAdmin);

                alreadyRouted = true; // đặt cờ TRƯỚC khi điều hướng
                routeAfterLogin(isAdmin);
            }).addOnFailureListener(e -> {
                setUiLoading(false);
                toast("Không lấy được claim: " + e.getMessage());
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        editUsernameOrEmail = findViewById(R.id.editUsernameOrEmail);
        editPassword        = findViewById(R.id.editPassword);
        btnLogin            = findViewById(R.id.btnLogin);
        btnGoToRegister     = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> login());

        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void login() {
        String id   = editUsernameOrEmail.getText().toString().trim();
        String pass = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pass)) {
            toast("Nhập username/email và mật khẩu");
            return;
        }

        setUiLoading(true);

        if (id.contains("@")) {
            // Đăng nhập bằng email
            signInWithEmail(id, pass);
        } else {
            // Đăng nhập bằng username: tra email trong Firestore
            db.collection("usernames").document(id.toLowerCase()).get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.exists()) {
                            setUiLoading(false);
                            toast("Username không tồn tại");
                            return;
                        }
                        String email = snap.getString("email");
                        if (TextUtils.isEmpty(email)) {
                            setUiLoading(false);
                            toast("Không tìm thấy email của username");
                            return;
                        }
                        signInWithEmail(email, pass);
                    })
                    .addOnFailureListener(e -> {
                        setUiLoading(false);
                        toast("Lỗi tra username: " + e.getMessage());
                    });
        }
    }

    private void signInWithEmail(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(t -> {
            if (!t.isSuccessful()) {
                setUiLoading(false);
                toast("Sai thông tin đăng nhập");
                return;
            }
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            if (u == null) {
                setUiLoading(false);
                toast("Lỗi đăng nhập");
                return;
            }

            // Lấy custom claim 'admin' rồi điều hướng
            u.getIdToken(true).addOnSuccessListener(result -> {
                boolean isAdmin = false;
                Map<String, Object> claims = result.getClaims();
                Object v = claims.get("admin");
                if (v instanceof Boolean) isAdmin = (Boolean) v;

                Log.d("CLAIMS", "claims=" + claims);
                Log.d("NAV", "routeAfterLogin (login) isAdmin=" + isAdmin);

                toast("Đăng nhập thành công!");
                alreadyRouted = true; // tránh onStart() redirect thêm lần nữa
                routeAfterLogin(isAdmin);
            }).addOnFailureListener(e -> {
                setUiLoading(false);
                toast("Không lấy được claim: " + e.getMessage());
            });
        });
    }

    private void routeAfterLogin(boolean isAdmin) {
        Log.d("NAV", "Starting " + (isAdmin ? "AdminActivity" : "StoreActivity"));
        Intent i = new Intent(this, isAdmin ? AdminActivity.class : StoreActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void setUiLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        // Có thể hiển thị ProgressBar nếu muốn
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}


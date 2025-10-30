//package com.example.book_store_mobileapp.ui.auth;
//
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Patterns;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.book_store_mobileapp.R;
//import com.google.firebase.auth.FirebaseAuth;
//
//public class PasswordRecoveryActivity extends AppCompatActivity {
//
//    private EditText edEmail;
//    private FirebaseAuth auth;
//
//    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_password_recovery);
//
//        edEmail = findViewById(R.id.edEmail);
//        Button btnSend = findViewById(R.id.btnSend);
//        auth = FirebaseAuth.getInstance();
//
//        btnSend.setOnClickListener(v -> {
//            String email = edEmail.getText().toString().trim();
//            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                edEmail.setError("Email không hợp lệ"); return;
//            }
//            auth.sendPasswordResetEmail(email) // KHÔNG dùng ActionCodeSettings -> mở web của Firebase
//                    .addOnSuccessListener(u ->
//                            Toast.makeText(this, "Đã gửi email đặt lại mật khẩu. Kiểm tra hộp thư.", Toast.LENGTH_LONG).show())
//                    .addOnFailureListener(e ->
//                            Toast.makeText(this, "Gửi thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show());
//        });
//    }
//}

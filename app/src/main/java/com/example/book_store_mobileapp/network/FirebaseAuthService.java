package com.example.book_store_mobileapp.network;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import com.example.book_store_mobileapp.network.ApiResponse;

import java.util.Map;

/**
 * FirebaseAuthService (bản khớp với ApiResponse của bạn)
 * -----------------------------------------------------
 * - Có đầy đủ: register, login, logout, currentUser, sendVerification,
 *   changePassword, reAuthenticate, changeEmail, getIdTokenClaims.
 */
public class FirebaseAuthService {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Callback chuẩn chung
    public interface Callback<T> { void onResult(ApiResponse<T> result); }

    /** Đăng ký tài khoản mới với email & password */
    public void register(String email, String password, Callback<FirebaseUser> cb) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> cb.onResult(ApiResponse.success(r.getUser())))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }

    /** Đăng nhập với email & password */
    public void login(String email, String password, Callback<FirebaseUser> cb) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> cb.onResult(ApiResponse.success(r.getUser())))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }

    /** Đăng xuất tài khoản hiện tại */
    public void logout() { auth.signOut(); }

    /** Lấy người dùng hiện tại (nếu có) */
    public FirebaseUser currentUser() { return auth.getCurrentUser(); }

    /** Gửi email xác minh */
    public void sendVerification(Callback<Void> cb) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) { cb.onResult(ApiResponse.error("User not logged in")); return; }
        u.sendEmailVerification()
                .addOnSuccessListener(v -> cb.onResult(ApiResponse.success(null)))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }

    /** Đổi mật khẩu (yêu cầu người dùng đã đăng nhập) */
    public void changePassword(String newPassword, Callback<Void> cb) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) { cb.onResult(ApiResponse.error("User not logged in")); return; }
        u.updatePassword(newPassword)
                .addOnSuccessListener(v -> cb.onResult(ApiResponse.success(null)))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }

    /** Re-authenticate bằng email & password hiện tại trước khi đổi thông tin nhạy cảm */
    public void reAuthenticate(String email, String password, Callback<Void> cb) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) { cb.onResult(ApiResponse.error("User not logged in")); return; }
        AuthCredential cred = EmailAuthProvider.getCredential(email, password);
        u.reauthenticate(cred)
                .addOnSuccessListener(v -> cb.onResult(ApiResponse.success(null)))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }




    public void getIdTokenClaims(boolean refresh, Callback<Map<String, Object>> cb) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) { cb.onResult(ApiResponse.error("User not logged in")); return; }
        u.getIdToken(refresh)
                .addOnSuccessListener((GetTokenResult r) -> cb.onResult(ApiResponse.success(r.getClaims())))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }

    // -------- helpers ----------
    private String safeMsg(Exception e) {
        return (e != null && e.getMessage() != null) ? e.getMessage() : "Unknown error";
    }
}

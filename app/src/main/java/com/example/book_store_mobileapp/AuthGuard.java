//package com.example.book_store_mobileapp;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import java.util.Arrays;
//
//public class AuthGuard {
//
//    // 🔐 UID của tài khoản Admin (giống Firestore Rules)
//    private static final String[] ADMIN_UIDS = {
//            "eCGRhJ3ZqwdvGrDkUHDDKQfGh9p1"
//    };
//
//    // Kiểm tra đã đăng nhập hay chưa
//    public static boolean isLoggedIn() {
//        return FirebaseAuth.getInstance().getCurrentUser() != null;
//    }
//
//    // Kiểm tra có phải Admin không
//    public static boolean isAdmin() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) return false;
//        return Arrays.asList(ADMIN_UIDS).contains(user.getUid());
//    }
//}

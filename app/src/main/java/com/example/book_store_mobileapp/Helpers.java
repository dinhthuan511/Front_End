package com.example.book_store_mobileapp;


import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Helpers {

    // ✅ Sinh mã giao dịch duy nhất (app_trans_id)
    public static String getAppTransId() {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        String date = format.format(new Date());
        int random = (int) (Math.random() * 1000000);
        return date + "_" + random;
    }

    // ✅ Sinh chuỗi HMAC SHA256
    public static String getMac(String key, String data) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        byte[] bytes = hmacSHA256.doFinal(data.getBytes("UTF-8"));
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }
        return hash.toString();
    }

    // ✅ Hàm hash SHA256 thông thường (nếu cần)
    public static String getSHA256(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

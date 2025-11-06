package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.book_store_mobileapp.data.CartItem;
import java.util.ArrayList;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class ZaloPayActivity extends AppCompatActivity {

    private String zpTransToken; // giữ token toàn cục
    private Intent checkoutIntent; // giữ lại intent gốc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        checkoutIntent = getIntent();
        zpTransToken = checkoutIntent.getStringExtra("zp_trans_token");

        long total = checkoutIntent.getLongExtra("total", 0);
        Log.d("ZALO_TOTAL", "💰 Tổng tiền nhận từ CheckoutActivity: " + total);
        Log.d("ZALO_TOKEN", "🎯 Token nhận từ CheckoutActivity: " + zpTransToken);

        if (zpTransToken == null || zpTransToken.isEmpty()) {
            Log.e("ZALO_PAY", "❌ Không có token được truyền vào!");
            Toast.makeText(this, "Không nhận được token thanh toán!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("ZALO_PAY", "🔹 Gọi payOrder() với token: " + zpTransToken);

        // Gọi SDK để thanh toán
        ZaloPaySDK.getInstance().payOrder(this, zpTransToken, "zalopay2554://app", new PayOrderListener() {
            @Override
            public void onPaymentSucceeded(String transactionId, String transToken, String appTransId) {
                Log.d("ZALO_PAY", "✅ Thanh toán thành công!");
                Toast.makeText(ZaloPayActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();

                OrderRepository.saveOrder(
                        getApplicationContext(),
                        checkoutIntent.getParcelableArrayListExtra("cartItems"),
                        checkoutIntent.getDoubleExtra("total", 0),
                        checkoutIntent.getStringExtra("name"),
                        checkoutIntent.getStringExtra("phone"),
                        checkoutIntent.getStringExtra("address"),
                        "ZaloPay",
                        "Đã thanh toán",
                        transactionId,
                        appTransId,
                        transToken
                );
            }

            @Override
            public void onPaymentCanceled(String transToken, String appTransId) {
                Log.w("ZALO_PAY", "⚠️ Người dùng hủy thanh toán");
                Toast.makeText(ZaloPayActivity.this, "Bạn đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onPaymentError(ZaloPayError zaloPayError, String transToken, String appTransId) {
                Log.e("ZALO_PAY", "❌ Lỗi thanh toán: " + zaloPayError.toString());
                Toast.makeText(ZaloPayActivity.this, "Lỗi thanh toán: " + zaloPayError, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("ZALO_PAY", "📩 onNewIntent() nhận callback từ ZaloPay");
        ZaloPaySDK.getInstance().onResult(intent);
    }
}

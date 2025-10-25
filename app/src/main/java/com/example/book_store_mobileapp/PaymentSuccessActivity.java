package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

public class PaymentSuccessActivity extends AppCompatActivity {

    private TextView tvMessage;
    private Button btnViewOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        tvMessage = findViewById(R.id.tvMessage);
        btnViewOrder = findViewById(R.id.btnViewOrder);

        tvMessage.setText("Thanh toán thành công 🎉\nCảm ơn bạn đã mua hàng!");

        btnViewOrder.setOnClickListener(v -> {
            String orderId = getIntent().getStringExtra("orderId");
            Intent intent = new Intent(PaymentSuccessActivity.this, OrderStatusActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        });
    }
}


package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PaymentProcessingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        tvStatus.setText("Đang xử lý thanh toán...");

        // Giả lập xử lý 3 giây
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(PaymentProcessingActivity.this, PaymentSuccessActivity.class);
            intent.putExtra("orderId", getIntent().getStringExtra("orderId"));
            startActivity(intent);
            finish();
        }, 3000);
    }
}

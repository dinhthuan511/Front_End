package com.example.book_store_mobileapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class AddressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ✅ sửa setContentView trỏ đúng layout của màn Địa chỉ
        setContentView(R.layout.activity_address);

        MaterialToolbar bar = findViewById(R.id.topAppBar);
        if (bar != null) {
            bar.setTitle("Địa chỉ");
            bar.setNavigationOnClickListener(v -> finish());
        }
    }
}

package com.example.book_store_mobileapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class AccountSecurityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_security);

        MaterialToolbar bar = findViewById(R.id.topAppBar);
        if (bar != null) {
            bar.setTitle("Tài khoản & Bảo mật");
            bar.setNavigationOnClickListener(v -> finish());
        }
    }
}

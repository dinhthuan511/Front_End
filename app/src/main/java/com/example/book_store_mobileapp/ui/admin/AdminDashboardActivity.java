package com.example.book_store_mobileapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.book_store_mobileapp.AdminOrdersActivity;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAuthService; // ✅ dùng service cho đồng bộ
import com.example.book_store_mobileapp.ui.auth.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialButton btnAddProduct, btnModifyProduct,btnManageOrders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setTitle("Hello, Admin");

        // Logout qua service cho thống nhất
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                new FirebaseAuthService().logout();   // ✅
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                return true;
            }
            return false;
        });

//        btnAddProduct    = findViewById(R.id.btnAddProduct);
        btnModifyProduct = findViewById(R.id.btnModifyProduct);
        btnManageOrders = findViewById(R.id.btnManageOrders);

//        btnAddProduct.setOnClickListener(v ->
//                startActivity(new Intent(this, AddEditProductActivity.class)));
        btnModifyProduct.setOnClickListener(v ->
                startActivity(new Intent(this, AdminProductsActivity.class)));
        btnManageOrders.setOnClickListener(v ->
                startActivity(new Intent(this, AdminOrdersActivity.class))); // 👈 mở trang quản lý đơn hàng
    }
}
package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.book_store_mobileapp.adapter.CartAdapter;
import com.example.book_store_mobileapp.data.CartManager;
import com.example.book_store_mobileapp.data.Book;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private ListView listView;
    private TextView txtTotal;
    private Button btnClear, btnCheckout;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) ->{
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        listView = findViewById(R.id.listViewCart);
        txtTotal = findViewById(R.id.txtTotal);
        btnClear = findViewById(R.id.btnClear);
        btnCheckout = findViewById(R.id.btnCheckout);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // Quay lại StoreActivity
            Intent intent = new Intent(CartActivity.this, StoreActivity.class);
            startActivity(intent);
            finish();
        });

        // 🟢 THÊM DỮ LIỆU MẪU NẾU GIỎ HÀNG ĐANG TRỐNG
        if (CartManager.getInstance().getCartItems().isEmpty()) {
//            addDemoData();
        }

        // Gắn adapter
        adapter = new CartAdapter(
                this,
                CartManager.getInstance().getCartItems(),
                this::updateTotal
        );
        listView.setAdapter(adapter);

        // Hiển thị tổng ban đầu
        updateTotal();

        // Nút xóa giỏ hàng
        btnClear.setOnClickListener(v -> {
            CartManager.getInstance().clearCart();
            adapter.notifyDataSetChanged();
            updateTotal();
            Toast.makeText(this, "Đã xóa toàn bộ giỏ hàng", Toast.LENGTH_SHORT).show();
        });

        // Nút thanh toán
        btnCheckout.setOnClickListener(v -> {
            double total = CartManager.getInstance().getTotalPrice();
            if (total == 0) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Thanh toán thành công: $" + String.format("%.2f", total), Toast.LENGTH_LONG).show();
                CartManager.getInstance().clearCart();
                adapter.notifyDataSetChanged();
                updateTotal();
            }
        });
    }

//    private void addDemoData() {
//        ArrayList<Book> demoBooks = new ArrayList<>();
//
//        demoBooks.add(new Book(
//                "1",
//                "Clean Code",
//                "Robert C. Martin",
//                "Cuốn sách kinh điển giúp lập trình viên viết code sạch, dễ đọc và bảo trì.",
//                "https://images-na.ssl-images-amazon.com/images/I/41xShlnTZTL._SX374_BO1,204,203,200_.jpg",
//                15.99
//        ));
//
//        demoBooks.add(new Book(
//                "2",
//                "Effective Java",
//                "Joshua Bloch",
//                "Tổng hợp hơn 70 hướng dẫn thực tiễn giúp bạn viết Java hiệu quả và an toàn hơn.",
//                "https://m.media-amazon.com/images/I/41zoxjP9lcL.jpg",
//                22.50
//        ));
//
//        demoBooks.add(new Book(
//                "3",
//                "Android Programming: Big Nerd Ranch Guide",
//                "Big Nerd Ranch",
//                "Hướng dẫn toàn diện về lập trình Android cho cả người mới và chuyên nghiệp.",
//                "https://m.media-amazon.com/images/I/51W9E4EupvL._SX260_.jpg",
//                30.00
//        ));
//
//        for (Book book : demoBooks) {
//            CartManager.getInstance().addToCart(book);
//        }
//    }


    private void updateTotal() {
        double total = CartManager.getInstance().getTotalPrice();
        txtTotal.setText("$" + String.format("%.2f", total));
    }
}

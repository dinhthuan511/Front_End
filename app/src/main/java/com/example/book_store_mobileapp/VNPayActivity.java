package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.book_store_mobileapp.adapter.CheckoutAdapter;
import com.example.book_store_mobileapp.data.CartItem;
import com.example.book_store_mobileapp.network.FirebaseCartService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.*;

public class VNPayActivity extends AppCompatActivity {

    private ArrayList<CartItem> cartItems;
    private CheckoutAdapter adapter;
    private TextView txtTotalVNPay, txtCustomerInfo;
    private EditText edtBankAccount;
    private Button btnPayVNPay;
    private ImageButton btnBack;

    private String name, phone, address, totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay);

        // Ánh xạ view
        txtTotalVNPay = findViewById(R.id.txtTotalVNPay);
        txtCustomerInfo = findViewById(R.id.txtCustomerInfo);
        edtBankAccount = findViewById(R.id.edtBankAccount);
        btnPayVNPay = findViewById(R.id.btnPayVNPay);
        btnBack = findViewById(R.id.btnBack);

        RecyclerView recyclerView = findViewById(R.id.recyclerVNPay);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nhận dữ liệu từ CheckoutActivity
        Intent intent = getIntent();
        cartItems = intent.getParcelableArrayListExtra("cartItems");
        totalAmount = intent.getStringExtra("totalAmount");
        name = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        address = intent.getStringExtra("address");

        // Hiển thị thông tin khách hàng
        txtCustomerInfo.setText("Tên: " + name + "\nSĐT: " + phone + "\nĐịa chỉ: " + address);
        txtTotalVNPay.setText(totalAmount);

        // Hiển thị danh sách hàng
        adapter = new CheckoutAdapter(this, cartItems);
        recyclerView.setAdapter(adapter);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Nút thanh toán VNPay
        btnPayVNPay.setOnClickListener(v -> handleVNPay());
    }

    // Xử lý khi bấm thanh toán VNPay
    private void handleVNPay() {
        String bankAccount = edtBankAccount.getText().toString().trim();
        if (bankAccount.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tài khoản ngân hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Giả lập thanh toán thành công
        Toast.makeText(this, "Thanh toán VNPay thành công!", Toast.LENGTH_SHORT).show();

        // 🔹 Lưu đơn hàng vào Firebase
        double total = 0;
        try {
            total = Double.parseDouble(totalAmount.replaceAll("[^0-9.]", ""));
        } catch (Exception ignored) { }

        saveOrderToFirebase(name, phone, address, bankAccount, total, cartItems);
    }

    // 🔹 Hàm lưu đơn hàng vào Firestore
    private void saveOrderToFirebase(String name, String phone, String address, String bankAccount, double totalAmount, List<CartItem> cartItems) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn cần đăng nhập trước khi đặt hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        Map<String, Object> order = new HashMap<>();
        order.put("userId", userId);
        order.put("name", name);
        order.put("phone", phone);
        order.put("address", address);
        order.put("paymentMethod", "VNPay");
        order.put("bankAccount", bankAccount);
        order.put("paymentStatus", "Đã thanh toán");
        order.put("status", "Đang vận chuyển");
        order.put("total", totalAmount + " VND");
        order.put("createdAt", FieldValue.serverTimestamp());

        // 🔹 Chuyển danh sách giỏ hàng thành danh sách Map
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("quantity", item.getQuantity());

            Map<String, Object> bookMap = new HashMap<>();
            if (item.getBook() != null) {
                bookMap.put("bookId", item.getBook().getBookId());
                bookMap.put("name", item.getBook().getName());
                bookMap.put("price", item.getBook().getPrice());
                bookMap.put("imageUrl", item.getBook().getImageUrl());
            }
            itemMap.put("book", bookMap);
            itemsList.add(itemMap);
        }
        order.put("items", itemsList);

        // 🔹 Lưu vào Firestore
        db.collection("orders").add(order)
                .addOnSuccessListener(doc -> {
                    String orderId = doc.getId();

                    // 🔹 Xóa giỏ hàng sau khi thanh toán thành công
                    FirebaseCartService cartService = new FirebaseCartService();
                    cartService.clearCart(userId, () -> {
                        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();

                        // 🔹 Sau khi xoá giỏ hàng, chuyển sang màn hình trạng thái đơn hàng
                        Intent intent = new Intent(this, PaymentProcessingActivity.class);
                        intent.putExtra("orderId", orderId);
                        startActivity(intent);
                        finish();
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lưu đơn hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}

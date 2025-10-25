package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.book_store_mobileapp.adapter.CheckoutAdapter;
import com.example.book_store_mobileapp.data.CartItem;
import com.example.book_store_mobileapp.data.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.*;

public class OrderStatusActivity extends AppCompatActivity {

    private TextView tvOrderId, tvStatus, tvCustomerInfo, tvTotalAmount;
    private ImageButton btnBack;
    private Button btnReturnHome;
    private RecyclerView recyclerOrderItems;
    private CheckoutAdapter adapter;
    private ArrayList<CartItem> orderItems = new ArrayList<>();
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvCustomerInfo = findViewById(R.id.tvCustomerInfo);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnReturnHome = findViewById(R.id.btnReturnHome);
        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);
        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        orderId = getIntent().getStringExtra("orderId");

        if (orderId != null && !orderId.isEmpty()) {
            loadOrderDetails(orderId);
        } else {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng!", Toast.LENGTH_SHORT).show();
        }
        btnReturnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoreActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadOrderDetails(String orderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        displayOrder(snapshot);
                    } else {
                        Toast.makeText(this, "Không tìm thấy đơn hàng!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void displayOrder(DocumentSnapshot snapshot) {
        String name = snapshot.getString("name");
        String phone = snapshot.getString("phone");
        String address = snapshot.getString("address");
        String paymentStatus = snapshot.getString("paymentStatus");
        String status = snapshot.getString("status");
        String total = snapshot.getString("total");
        List<Map<String, Object>> items = (List<Map<String, Object>>) snapshot.get("items");

        tvOrderId.setText("Mã đơn hàng: " + snapshot.getId());
        tvStatus.setText("Thanh toán: " + paymentStatus + "\nVận chuyển: " + status);
        tvCustomerInfo.setText("Tên: " + name + "\nSĐT: " + phone + "\nĐịa chỉ: " + address);
        tvTotalAmount.setText("Tổng tiền: " + total);

        // Hiển thị danh sách sản phẩm
        if (items != null) {
            orderItems.clear();
            for (Map<String, Object> map : items) {
                try {
                    Map<String, Object> bookMap = (Map<String, Object>) map.get("book");
                    if (bookMap == null) continue;

                    CartItem item = new CartItem();
                    item.setQuantity(((Long) map.get("quantity")).intValue());

                    Book book = new Book();
                    book.setBookId((String) bookMap.get("bookId"));
                    book.setName((String) bookMap.get("name"));
                    book.setImageUrl((String) bookMap.get("imageUrl"));
                    Object priceObj = bookMap.get("price");
                    if (priceObj instanceof Double)
                        book.setPrice((Double) priceObj);
                    else if (priceObj instanceof Long)
                        book.setPrice(((Long) priceObj).doubleValue());
                    item.setBook(book);

                    orderItems.add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            adapter = new CheckoutAdapter(this, orderItems);
            recyclerOrderItems.setAdapter(adapter);
        }
    }
}

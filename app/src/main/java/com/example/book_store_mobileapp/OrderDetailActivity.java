package com.example.book_store_mobileapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.book_store_mobileapp.adapter.OrderItemAdapter;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.data.OrderItem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvStatus, tvCustomerInfo, tvTotalAmount;
    private RecyclerView recyclerView;
    private OrderItemAdapter adapter;
    private List<OrderItem> orderItemList = new ArrayList<>();
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvCustomerInfo = findViewById(R.id.tvCustomerInfo);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        recyclerView = findViewById(R.id.recyclerOrderDetail);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemAdapter(orderItemList);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        String orderId = getIntent().getStringExtra("orderId");
        if (orderId != null && !orderId.isEmpty()) {
            loadOrderDetail(orderId);
        } else {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOrderDetail(String orderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(this::displayOrder)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void displayOrder(DocumentSnapshot snapshot) {
        if (!snapshot.exists()) {
            Toast.makeText(this, "Đơn hàng không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = snapshot.getString("name");
        String phone = snapshot.getString("phone");
        String address = snapshot.getString("address");
        String paymentStatus = snapshot.getString("paymentStatus"); // "Đã thanh toán" / "Chưa thanh toán"
        String status = snapshot.getString("status"); // "Đã giao", "Đang giao", "Chờ xác nhận"
        Double total = snapshot.getDouble("total");

        List<Map<String, Object>> items = (List<Map<String, Object>>) snapshot.get("items");
        orderItemList.clear();

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                Map<String, Object> bookMap = (Map<String, Object>) itemMap.get("book");
                if (bookMap != null) {
                    Book book = new Book();
                    book.setName((String) bookMap.get("name"));
                    if (bookMap.get("price") instanceof Number) {
                        book.setPrice(((Number) bookMap.get("price")).doubleValue());
                    }
//                    book.setImageUrl((String) bookMap.get("imageUrl"));

                    int quantity = 1;
                    if (itemMap.get("quantity") instanceof Number) {
                        quantity = ((Number) itemMap.get("quantity")).intValue();
                    }

                    orderItemList.add(new OrderItem(book, quantity));
                }
            }
        }

        adapter.notifyDataSetChanged();

        tvOrderId.setText("Mã đơn hàng: " + snapshot.getId());
        tvStatus.setText("Thanh toán: " + paymentStatus + "\nVận chuyển: " + status);
        tvCustomerInfo.setText("Tên: " + name + "\nSĐT: " + phone + "\nĐịa chỉ: " + address);
        tvTotalAmount.setText("Tổng tiền: " + FormatUtils.formatCurrency(total));

        // 🎨 Đổi màu trạng thái
        if ("Đã thanh toán".equals(paymentStatus) && "Đã giao".equals(status)) {
            // ✅ Hoàn tất đơn
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            // 🟠 Các trạng thái khác (chưa hoàn thiện)
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        }
    }
}

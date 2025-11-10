package com.example.book_store_mobileapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.book_store_mobileapp.adapter.CheckoutAdapter;
import com.example.book_store_mobileapp.data.CartItem;
import com.example.book_store_mobileapp.data.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.*;

public class OrderStatusActivity extends AppCompatActivity {

    private TextView tvOrderId, tvStatus, tvCustomerInfo, tvTotalAmount;
    private RecyclerView recyclerOrderItems;
    private CheckoutAdapter adapter;
    private ArrayList<CartItem> orderItems = new ArrayList<>();
    private String orderId;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private Button btnCancelOrder;

    private ListenerRegistration orderListener;
    private String paymentStatus, deliveryStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvCustomerInfo = findViewById(R.id.tvCustomerInfo);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);
        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        db = FirebaseFirestore.getInstance();

        orderId = getIntent().getStringExtra("orderId");

        if (orderId != null && !orderId.isEmpty()) {
            listenOrderChanges(orderId);
        } else {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng!", Toast.LENGTH_SHORT).show();
        }

        // 👉 Xử lý sự kiện hủy đơn hàng
        btnCancelOrder.setOnClickListener(v -> showCancelConfirmDialog());
    }

    private void listenOrderChanges(String orderId) {
        orderListener = db.collection("orders").document(orderId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi khi cập nhật đơn: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        displayOrder(snapshot);
                    }
                });
    }

    private void displayOrder(DocumentSnapshot snapshot) {
        String name = snapshot.getString("name");
        String phone = snapshot.getString("phone");
        String address = snapshot.getString("address");
        paymentStatus = snapshot.getString("paymentStatus");
        deliveryStatus = snapshot.getString("status");
        Double total = snapshot.getDouble("total");

        tvOrderId.setText("Mã đơn hàng: " + snapshot.getId());
        tvCustomerInfo.setText("Tên: " + name + "\nSĐT: " + phone + "\nĐịa chỉ: " + address);
        tvTotalAmount.setText("Số tiền phải trả: " + FormatUtils.formatCurrency(total));

        // ✅ Hiển thị màu chữ theo trạng thái
        String paymentText = "Thanh toán: " + paymentStatus;
        String deliveryText = "Vận chuyển: " + deliveryStatus;
        tvStatus.setText(paymentText + "\n" + deliveryText);

        if ((paymentStatus != null && paymentStatus.equalsIgnoreCase("Đã thanh toán")) ||
                (deliveryStatus != null && deliveryStatus.equalsIgnoreCase("Đã giao"))) {
            tvStatus.setTextColor(Color.parseColor("#2E7D32")); // xanh
        } else {
            tvStatus.setTextColor(Color.parseColor("#F57C00")); // cam
        }

        // ✅ Nếu đã giao hoặc đang giao thì không cho hủy
        if (deliveryStatus != null &&
                (deliveryStatus.equalsIgnoreCase("Đang giao") ||
                        deliveryStatus.equalsIgnoreCase("Đã giao") ||
                        deliveryStatus.equalsIgnoreCase("Đã hủy"))) {
            btnCancelOrder.setEnabled(false);
            btnCancelOrder.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        } else {
            btnCancelOrder.setEnabled(true);
            btnCancelOrder.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
        }

        // ✅ Hiển thị danh sách sản phẩm
        List<Map<String, Object>> items = (List<Map<String, Object>>) snapshot.get("items");
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

            if (adapter == null) {
                adapter = new CheckoutAdapter(this, orderItems);
                recyclerOrderItems.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    // 🧩 Hộp thoại xác nhận hủy
    private void showCancelConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Có, hủy đơn", (dialog, which) -> cancelOrder())
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // 🧩 Hàm thực hiện hủy đơn hàng trong Firestore
    private void cancelOrder() {
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("orders").document(orderId)
                .update("status", "Đã hủy", "paymentStatus", "Đã hủy")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đơn hàng đã được hủy thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi hủy đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}

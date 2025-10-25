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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.*;

public class CheckoutActivity extends AppCompatActivity {

    private ArrayList<CartItem> cartItems;
    private CheckoutAdapter adapter;
    private TextView txtTotal;
    private EditText edtName, edtPhone, edtAddress;
    private RadioGroup paymentMethodGroup;
    private Button btnConfirm;
    private ImageButton btnBack;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Ánh xạ view
        txtTotal = findViewById(R.id.txtTotal);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        RecyclerView recyclerView = findViewById(R.id.recyclerCheckout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nhận cartItems từ Intent
        cartItems = getIntent().getParcelableArrayListExtra("cartItems");
        if (cartItems == null) cartItems = new ArrayList<>();

        adapter = new CheckoutAdapter(this, cartItems);
        recyclerView.setAdapter(adapter);

        // Tính tổng tiền
        total = 0;
        for (CartItem item : cartItems) {
            total += item.getBook().getPrice() * item.getQuantity();
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        txtTotal.setText("Tổng cộng: " + formatter.format(total) + " VND");

        btnBack.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> handleConfirm());
    }

    private void handleConfirm() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedMethodId = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedMethodId == -1) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedMethod = findViewById(selectedMethodId);
        String method = selectedMethod.getText().toString();

        if (method.contains("Tiền mặt")) {
            saveOrderToFirebase(name, phone, address, "Tiền mặt");
        } else {
            // Chuyển sang trang VNPay
            Intent intent = new Intent(this, VNPayActivity.class);
            intent.putParcelableArrayListExtra("cartItems", cartItems);
            intent.putExtra("totalAmount", total);
            intent.putExtra("name", name);
            intent.putExtra("phone", phone);
            intent.putExtra("address", address);
            startActivity(intent);
        }
    }

    // 🟢 Hàm lưu đơn hàng vào Firestore
    private void saveOrderToFirebase(String name, String phone, String address, String paymentMethod) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> order = new HashMap<>();
            order.put("userId", userId);
            order.put("name", name);
            order.put("phone", phone);
            order.put("address", address);
            order.put("total", total + " VND");
            order.put("paymentMethod", paymentMethod);
            order.put("paymentStatus", "Chưa thanh toán");
            order.put("status", "Đang vận chuyển");
            order.put("createdAt", FieldValue.serverTimestamp());

            // Danh sách sản phẩm
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (CartItem item : cartItems) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("quantity", item.getQuantity());

                Map<String, Object> bookMap = new HashMap<>();
                bookMap.put("bookId", item.getBook().getBookId() != null ? item.getBook().getBookId() : "");
                bookMap.put("name", item.getBook().getName() != null ? item.getBook().getName() : "");
                bookMap.put("price", item.getBook().getPrice());
                bookMap.put("imageUrl", item.getBook().getImageUrl() != null ? item.getBook().getImageUrl() : "");

                itemMap.put("book", bookMap);
                itemsList.add(itemMap);
            }
            order.put("items", itemsList);

            db.collection("orders").add(order)
                    .addOnSuccessListener(docRef -> {
                        String orderId = docRef.getId();
                        // 🔹 Gọi clearCart sau khi lưu đơn hàng thành công
                        FirebaseCartService cartService = new FirebaseCartService();
                        cartService.clearCart(userId, () -> {
                            Toast.makeText(this, "Đặt hàng thành công!.", Toast.LENGTH_SHORT).show();
                            // Sau khi xóa giỏ hàng, chuyển sang trang trạng thái đơn hàng
                            Intent intent = new Intent(this, PaymentProcessingActivity.class);
                            intent.putExtra("orderId", orderId);
                            startActivity(intent);
                            finish();
                        });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi lưu đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}

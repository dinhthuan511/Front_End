package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.book_store_mobileapp.adapter.CartAdapter;
import com.example.book_store_mobileapp.data.AppNotification;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.data.CartItem;
import com.example.book_store_mobileapp.data.NotificationManager;
import com.example.book_store_mobileapp.network.FirebaseCartService;
import com.google.firebase.auth.FirebaseAuth;
import com.example.book_store_mobileapp.network.CartCountRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private ListView listView;
    private TextView txtTotal;
    private Button btnCheckout, btnClearAll; // ✅ thêm nút xóa toàn bộ
    private ImageButton btnBack;
    private CartAdapter adapter;
    private ArrayList<CartItem> cartItems = new ArrayList<>();
    private FirebaseCartService cartService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        listView = findViewById(R.id.listViewCart);
        txtTotal = findViewById(R.id.txtTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);
        btnClearAll = findViewById(R.id.btnClearAll); // ✅ ánh xạ nút xóa toàn bộ

        cartService = new FirebaseCartService();

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, StoreActivity.class));
            finish();
        });

        adapter = new CartAdapter(this, cartItems, this::updateTotal);
        listView.setAdapter(adapter);
        checkLoginStatus();
        loadCartFromFirebase();

        // ✅ Sự kiện nhấn nút thanh toán
        btnCheckout.setOnClickListener(v -> {
            double totalAmount = calculateTotal();
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("totalAmount", totalAmount);
            intent.putParcelableArrayListExtra("cartItems", cartItems);
            startActivity(intent);
        });

        // ✅ Sự kiện nhấn nút xóa toàn bộ
        btnClearAll.setOnClickListener(v -> confirmClearCart());
        // Add in-app notification
        NotificationManager.getInstance().addNotification(
                this,
                new AppNotification(
                        "cart_cleared_" + System.currentTimeMillis(),
                        "Cart Cleared",
                        "You cleared all items from your cart.",
                        "cart_cleared"
                )
        );

// Update system notification (cart is now empty, so it will be cancelled)
        NotificationHelper.updateCartSystemNotification(this);

// refresh repository so UI badges update immediately
        CartCountRepository.getInstance().refreshCartCount();


    }

    /** 🔹 Kiểm tra đăng nhập để hiển thị/ẩn nút */
    private void checkLoginStatus() {
        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        btnCheckout.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
        btnClearAll.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
    }


    private void loadCartFromFirebase() {
        Log.d("CartActivity", "🔄 Bắt đầu tải giỏ hàng từ Firebase...");

        if (cartService == null || cartService.getCartRef() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        cartService.getCartRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                cartItems.clear();
                QuerySnapshot snapshot = task.getResult();

                if (snapshot != null && !snapshot.isEmpty()) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        try {
                            String cartItemId = doc.getId();
                            String bookId = doc.getString("bookId");
                            int quantity = doc.getLong("quantity") != null
                                    ? doc.getLong("quantity").intValue()
                                    : 1;

                            Book book = new Book(
                                    bookId,
                                    doc.getString("productName"),
                                    doc.getString("author"),
                                    doc.getString("briefDescription"),
                                    doc.getString("fullDescription"),
                                    doc.getLong("categoryId"),
                                    doc.getString("imageURL"),
                                    doc.getString("isbn"),
                                    doc.getDouble("price"),
                                    doc.getLong("stock"),
                                    doc.getString("technicalSpecifications")
                            );

                            CartItem item = new CartItem(cartItemId, book, quantity);
                            item.setCartId(cartItemId);
                            cartItems.add(item);

                        } catch (Exception e) {
                            Log.e("CartActivity", "⚠️ Lỗi khi đọc document: " + doc.getId(), e);
                        }
                    }
                } else {
                    Toast.makeText(this, "Giỏ hàng trống.", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
                updateTotal();

            } else {
                Toast.makeText(this, "Không thể tải giỏ hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 🔹 Hiển thị hộp thoại xác nhận xóa toàn bộ */
    private void confirmClearCart() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đã trống.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa toàn bộ giỏ hàng?")
                .setMessage("Bạn có chắc muốn xóa toàn bộ sản phẩm khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> clearCartFromFirebase())
                .setNegativeButton("Hủy", null)
                .show();
    }

    /** 🔹 Thực hiện xóa toàn bộ giỏ hàng trong Firebase */
    private void clearCartFromFirebase() {
        cartService.getCartRef().get().addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                doc.getReference().delete();
            }
            cartItems.clear();
            adapter.notifyDataSetChanged();
            updateTotal();
            Toast.makeText(this, "Đã xóa toàn bộ giỏ hàng.", Toast.LENGTH_SHORT).show();
            // Add in-app notification
            NotificationManager.getInstance().addNotification(
                    this,
                    new AppNotification(
                            "cart_cleared_" + System.currentTimeMillis(),
                            "Giỏ hàng đã xóa",
                            "Bạn đã xóa tất cả sản phẩm khỏi giỏ hàng.",
                            "cart_cleared"
                    )
            );
            // Update system notification (cart is now empty, so it will be cancelled)
            NotificationHelper.updateCartSystemNotification(this);
            // refresh repository so UI badges update immediately
            CartCountRepository.getInstance().refreshCartCount();
        });
    }

    /** 🔹 Cập nhật tổng tiền */
    private void updateTotal() {
        double total = calculateTotal();
        java.text.NumberFormat formatter =
                java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        txtTotal.setText("Tổng cộng: " + formatter.format(total) + " VND");
    }

    /** 🔹 Tính tổng giá trị giỏ hàng */
    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getBook().getPrice() * item.getQuantity();
        }
        return total;
    }
}
package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.book_store_mobileapp.adapter.CartAdapter;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.data.CartItem;
import com.example.book_store_mobileapp.network.FirebaseCartService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private ListView listView;
    private TextView txtTotal;
    private Button btnClear, btnCheckout;
    private ImageButton btnBack ;
    private CartAdapter adapter;

    private ArrayList<CartItem> cartItems = new ArrayList<>();
    private FirebaseCartService cartService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        listView = findViewById(R.id.listViewCart);
        txtTotal = findViewById(R.id.txtTotal);
        btnClear = findViewById(R.id.btnClear);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);

        cartService = new FirebaseCartService();

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, StoreActivity.class));
            finish();
        });

        adapter = new CartAdapter(this, cartItems, this::updateTotal);
        listView.setAdapter(adapter);

        loadCartFromFirebase();

        btnCheckout.setOnClickListener(v -> {
            double totalAmount = calculateTotal(); // ✅ Lấy tổng tiền thực tế
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("totalAmount", totalAmount);
            intent.putParcelableArrayListExtra("cartItems", cartItems);
            startActivity(intent);
        });
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
                            // 🔹 Lấy ID của document (cartItemId)
                            String cartItemId = doc.getId();

                            String bookId = doc.getString("bookId");
                            int quantity = doc.getLong("quantity") != null
                                    ? doc.getLong("quantity").intValue()
                                    : 1;

                            // 🔹 Tạo đối tượng Book
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

                            // 🔹 Gán cartItemId vào CartItem để dùng khi xóa
                            CartItem item = new CartItem(cartItemId, book, quantity);
                            item.setCartId(cartItemId); // ✅ Cực kỳ quan trọng
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



    private void updateTotal() {
        double total = calculateTotal();
        // Định dạng theo kiểu Việt Nam
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        txtTotal.setText(formatter.format(total) + " VND");
    }

    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getBook().getPrice() * item.getQuantity();
        }
        return total;
    }
}

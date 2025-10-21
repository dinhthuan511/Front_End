package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.book_store_mobileapp.adapter.CartAdapter;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.network.FirebaseCartService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private ListView listView;
    private TextView txtTotal;
    private Button btnClear, btnCheckout;
    private ImageButton btnBack;
    private CartAdapter adapter;
    private ArrayList<Book> cartItems = new ArrayList<>();
    private FirebaseCartService cartService; // ✅

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Ánh xạ view
        listView = findViewById(R.id.listViewCart);
        txtTotal = findViewById(R.id.txtTotal);
        btnClear = findViewById(R.id.btnClear);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);

        // Khởi tạo service Firebase
        cartService = new FirebaseCartService();

        // Quay lại StoreActivity
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, StoreActivity.class));
            finish();
        });

        // Adapter
        adapter = new CartAdapter(this, cartItems, this::updateTotal);
        listView.setAdapter(adapter);

        // Tải dữ liệu từ Firebase
        loadCartFromFirebase();

        // Xóa toàn bộ giỏ hàng
        btnClear.setOnClickListener(v -> {
            cartService.clearCart(
                    () -> {
                        cartItems.clear();
                        adapter.notifyDataSetChanged();
                        updateTotal();
                        Toast.makeText(this, "Đã xóa toàn bộ giỏ hàng", Toast.LENGTH_SHORT).show();
                    },
                    () -> Toast.makeText(this, "Lỗi khi xóa giỏ hàng", Toast.LENGTH_SHORT).show()
            );
        });

        // Thanh toán
        btnCheckout.setOnClickListener(v -> {
            double total = calculateTotal();
            if (total == 0) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Thanh toán thành công: $" + String.format("%.2f", total), Toast.LENGTH_LONG).show();

            cartService.clearCart(
                    () -> {
                        cartItems.clear();
                        adapter.notifyDataSetChanged();
                        updateTotal();
                    },
                    () -> Toast.makeText(this, "Lỗi khi xóa giỏ hàng sau thanh toán", Toast.LENGTH_SHORT).show()
            );
        });
    }

    // 🟢 Hàm tải dữ liệu Firestore
    private void loadCartFromFirebase() {
        Log.d("CartActivity", "🔄 Bắt đầu tải giỏ hàng từ Firebase...");

        // Kiểm tra nếu chưa đăng nhập
        if (cartService == null || cartService.getCartRef() == null) {
            Log.e("CartActivity", "❌ Không thể tải giỏ hàng: cartService hoặc userId null.");
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        cartService.getCartRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("CartActivity", "✅ Tải giỏ hàng thành công từ Firestore.");
                cartItems.clear();
                QuerySnapshot snapshot = task.getResult();

                if (snapshot != null && !snapshot.isEmpty()) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        try {
                            // Ghi log từng item
                            Log.d("CartActivity", "📘 Đang đọc item: " + doc.getId());

                            Book book = new Book(
                                    doc.getString("bookId"),
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

                            Long q = doc.getLong("quantity");
                            book.setQuantity(q != null ? q.intValue() : 1);

                            cartItems.add(book);

                            Log.d("CartActivity", "🛒 Thêm vào danh sách: " + book.getName() + " - SL: " + book.getQuantity());
                        } catch (Exception e) {
                            Log.e("CartActivity", "⚠️ Lỗi khi đọc document: " + doc.getId(), e);
                        }
                    }
                } else {
                    Log.w("CartActivity", "⚠️ Giỏ hàng trống hoặc snapshot null.");
                    Toast.makeText(this, "Giỏ hàng của bạn đang trống.", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
                updateTotal();
            } else {
                Log.e("CartActivity", "❌ Không thể tải giỏ hàng từ Firestore.", task.getException());
                Toast.makeText(this, "Không thể tải giỏ hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // 🧮 Cập nhật tổng tiền
    private void updateTotal() {
        txtTotal.setText("$" + String.format("%.2f", calculateTotal()));
    }

    private double calculateTotal() {
        double total = 0;
        for (Book b : cartItems) {
            total += b.getPrice() * b.getQuantity();
        }
        return total;
    }
}

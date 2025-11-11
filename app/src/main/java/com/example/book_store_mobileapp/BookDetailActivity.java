package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.book_store_mobileapp.data.AppNotification;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.data.NotificationManager;
import com.example.book_store_mobileapp.network.CartCountRepository;
import com.example.book_store_mobileapp.network.FirebaseCartService;
import com.example.book_store_mobileapp.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView detailBookImage;
    private TextView detailBookName, detailBookAuthor, detailBookDescription,detailBookPrice, detailBookTechnicalSpecifications;
    private EditText etxtQuantity;
    private TextView imageOutOfStockOverlay;
    private LinearLayout addToCartRow;
    private Button btnAddToCart;
    private ImageButton btnBack, btnMinus, btnPlus;
    private FirebaseCartService cartService;

    private int currentQuantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) ->{
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        imageOutOfStockOverlay = findViewById(R.id.imageOutOfStockOverlay);
        detailBookImage = findViewById(R.id.bookImage);
        detailBookName = findViewById(R.id.bookName);
        detailBookAuthor = findViewById(R.id.bookAuthor);
        detailBookDescription = findViewById(R.id.bookDescription);
        detailBookTechnicalSpecifications = findViewById(R.id.bookTechnicalSpecifications);
        detailBookPrice = findViewById(R.id.bookPrice);
        addToCartRow = findViewById(R.id.addToCartRow);
        etxtQuantity = findViewById(R.id.etxtQuantity);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBack = findViewById(R.id.btnBack);


        // Initialize FirebaseCartService
        cartService = new FirebaseCartService();

        // Get Book from intent
        Book book = getIntent().getParcelableExtra("SELECTED_BOOK");

        // Check if book is not null
        if(book != null){
            // Set book data
            detailBookName.setText(book.getName());
            detailBookAuthor.setText("Tác giả: " + book.getAuthor());
            detailBookDescription.setText(book.getFullDescription());
            detailBookTechnicalSpecifications.setText(book.getTechnicalSpecifications() + "\nISBN: " + book.getIsbn());
            // Format the price
            NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedPrice = format.format(book.getPrice());
            detailBookPrice.setText(formattedPrice + " VNĐ");
            // Set image with Glide
            Glide.with(this)
                    .load(book.getImageUrl())
                    .error(android.R.drawable.dark_header)
                    .into(detailBookImage);

            if(book.getStock() != null && book.getStock() <= 0) {
                addToCartRow.setVisibility(View.GONE);
                imageOutOfStockOverlay.setVisibility(View.VISIBLE);
                detailBookImage.setAlpha(0.25f);
            } else {
                addToCartRow.setVisibility(View.VISIBLE);
                imageOutOfStockOverlay.setVisibility(View.GONE);
            }

            etxtQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String quantityStr = s.toString();
                    if (quantityStr.isEmpty()) {
                        currentQuantity = 1;
                        return;
                    }
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        if (quantity < 1) {
                            currentQuantity = 1;
                            etxtQuantity.setText(String.valueOf(currentQuantity));
                            etxtQuantity.setSelection(etxtQuantity.getText().length());
                        } else if (book.getStock() != null && quantity > book.getStock()) {
                            currentQuantity = book.getStock().intValue();
                            etxtQuantity.setText(String.valueOf(currentQuantity));
                            etxtQuantity.setSelection(etxtQuantity.getText().length());
//                            Toast.makeText(BookDetailActivity.this, "Số lượng không được vượt quá số lượng trong kho", Toast.LENGTH_SHORT).show();
                        } else {
                            currentQuantity = quantity;
                        }
                    } catch (NumberFormatException e) {
                        currentQuantity = 1;
                        etxtQuantity.setText(String.valueOf(currentQuantity));
                        etxtQuantity.setSelection(etxtQuantity.getText().length());
                    }
                }
            });

            btnMinus.setOnClickListener(v -> {
                if (currentQuantity > 1) {
                    currentQuantity--;
                    etxtQuantity.setText(String.valueOf(currentQuantity));
                }
            });

            btnPlus.setOnClickListener(v -> {
                if(book.getStock() != null && currentQuantity < book.getStock()){
                    currentQuantity++;
                    etxtQuantity.setText(String.valueOf(currentQuantity));
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if(FirebaseAuth.getInstance().getCurrentUser() == null){
                    // Nếu chưa đăng nhập, chuyển về trang Login và thông báo cần đăng nhập
                    Intent intent = new Intent(BookDetailActivity.this, LoginActivity.class);
                    Toast.makeText(BookDetailActivity.this, "Bạn cần đăng nhập để thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                } else {

                    if (book.getStock() != null && currentQuantity > book.getStock()) {
                        Toast.makeText(BookDetailActivity.this,
                                "Số lượng vượt quá tồn kho! Chỉ còn " + book.getStock() + " cuốn.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    btnAddToCart.setEnabled(false);
                    Log.d("BookDetailActivity", "Người dùng bấm Thêm vào giỏ hàng: " + book.getName());
                    cartService.addToCart(book, currentQuantity,
                            () -> {
                                Log.d("BookDetailActivity", "Thêm thành công: " + book.getName());
                                Toast.makeText(BookDetailActivity.this, book.getName() + " đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();

                                // ✅ Thêm thông báo tại đây
                                NotificationManager.getInstance().addNotification(
                                        BookDetailActivity.this,
                                        new AppNotification(
                                                "cart_add_" + System.currentTimeMillis(),
                                                "Item Added to Cart",
                                                "You added \"" + book.getName() + "\" to your cart.",
                                                "cart"
                                        )
                                );

                                NotificationHelper.createCartChannel(BookDetailActivity.this);
                                NotificationHelper.updateCartSystemNotification(BookDetailActivity.this);

                                CartCountRepository.getInstance().refreshCartCount();
                                // ✅ END add notification

                                finish();
                            },
                            () -> {
                                Log.e("BookDetailActivity", "Thêm thất bại: " + book.getName());
                                btnAddToCart.setEnabled(true);
                                btnAddToCart.setText("Add to cart");
                                Toast.makeText(BookDetailActivity.this, "Hết hàng", Toast.LENGTH_SHORT).show();
                            }
                    );
                }
            });
        } else {
            // Handle book data null
            Toast.makeText(this, "Không tìm thấy dữ liệu sách!", Toast.LENGTH_LONG).show();
            finish(); // Close activity
        }

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
}

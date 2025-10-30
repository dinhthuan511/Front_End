package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.network.FirebaseCartService;

import java.text.NumberFormat;
import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView detailBookImage;
    private TextView detailBookName, detailBookAuthor, detailBookDescription,detailBookPrice, detailBookTechnicalSpecifications, txtQuantity;
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
        txtQuantity = findViewById(R.id.txtQuantity);
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
            detailBookAuthor.setText("Author: " + book.getAuthor());
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

            btnMinus.setOnClickListener(v -> {
                if (currentQuantity > 1) {
                    currentQuantity--;
                    txtQuantity.setText(String.valueOf(currentQuantity));
                }
            });

            btnPlus.setOnClickListener(v -> {
                if(currentQuantity < book.getStock()){
                    currentQuantity++;
                    txtQuantity.setText(String.valueOf(currentQuantity));
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                btnAddToCart.setEnabled(false);
                btnAddToCart.setText("Adding...");
                Log.d("BookDetailActivity", "Người dùng bấm Thêm vào giỏ hàng: " + book.getName());
                cartService.addToCart(book, currentQuantity,
                        () -> {
                            Log.d("BookDetailActivity", "Thêm thành công: " + book.getName());
                            Toast.makeText(BookDetailActivity.this, book.getName() + " đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                            finish();
                        },
                        () -> {
                            Log.e("BookDetailActivity", "Thêm thất bại: " + book.getName());
                            btnAddToCart.setEnabled(true);
                            btnAddToCart.setText("Add to cart");
                            Toast.makeText(BookDetailActivity.this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                );
            });
        } else {
            // Handle book data null
            Toast.makeText(this, "Book data is not found!", Toast.LENGTH_LONG).show();
            finish(); // Close activity
        }

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
}
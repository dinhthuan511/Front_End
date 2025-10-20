package com.example.book_store_mobileapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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
    private TextView detailBookName, detailBookAuthor, detailBookDescription, detailBookPrice;
    private Button btnAddToCart;
    private FirebaseCartService cartService;

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
        detailBookImage = findViewById(R.id.bookImage);
        detailBookName = findViewById(R.id.bookName);
        detailBookAuthor = findViewById(R.id.bookAuthor);
        detailBookDescription = findViewById(R.id.bookDescription);
        detailBookPrice = findViewById(R.id.bookPrice);
        btnAddToCart = findViewById(R.id.btnAddToCart);

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
            // Format the price
            NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedPrice = format.format(book.getPrice());
            detailBookPrice.setText(formattedPrice + " VNĐ");
            // Set image with Glide
            Glide.with(this)
                    .load(book.getImageUrl())
                    .error(android.R.drawable.dark_header)
                    .into(detailBookImage);

            // Set event listener for Add to cart button
            btnAddToCart.setOnClickListener(v -> {
                // check token cua user neu chua co chuyen qua trang login


                // Do Add to cart logic
                cartService.addToCart(book, 1,
                        () -> Toast.makeText(BookDetailActivity.this, book.getName() + " đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show(),
                        () -> Toast.makeText(BookDetailActivity.this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                );
            });
        } else {
            // Handle book data null
            Toast.makeText(this, "Book data is not found!", Toast.LENGTH_LONG).show();
            finish(); // Close activity
        }
    }
}
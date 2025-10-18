package com.example.book_store_mobileapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.book_store_mobileapp.data.CartManager;

import java.text.NumberFormat;
import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView detailBookImage;
    private TextView detailBookName, detailBookAuthor, detailBookDescription, detailBookPrice;
    private Button btnMinus, btnPlus, btnAddToCart;
    private EditText itemQuantity;

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
        itemQuantity = findViewById(R.id.itemQuantity);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // Get Book from intent
        Book book = getIntent().getParcelableExtra("SELECTED_BOOK");

        // Check if book is not null
        if(book != null){
            // Set book data
            detailBookName.setText(book.getProductName());
            detailBookAuthor.setText("Author: " + book.getAuthor());
            detailBookDescription.setText(book.getFullDescription());
            // Format the price
            NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedPrice = format.format(book.getPrice());
            detailBookPrice.setText(formattedPrice + " VNĐ");
            // Set image with Glide
            Glide.with(this)
                    .load(book.getImageURL())
                    .error(R.drawable.book_sample_background)
                    .into(detailBookImage);

            // Item quantity control
            btnMinus.setOnClickListener(v-> {
                int quantity = getQuantityFromEditText();
                if(quantity > 1){
                    quantity--;
                    itemQuantity.setText(String.valueOf(quantity));
                }
            });
            btnPlus.setOnClickListener(v-> {
                int quantity = getQuantityFromEditText();
                if(quantity < 999){
                    quantity++;
                    itemQuantity.setText(String.valueOf(quantity));
                }
            });
            itemQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().isEmpty() || s.toString().equals("0")){
                        // Temporary disable listener to avoid infinite looping
                        itemQuantity.removeTextChangedListener(this);
                        itemQuantity.setText("1");
                        itemQuantity.selectAll();
                        // Add listener back
                        itemQuantity.addTextChangedListener(this);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // not needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // not needed
                }
            });

            // Set event listener for Add to cart button
            btnAddToCart.setOnClickListener(v -> {
                // Do Add to cart logic
                int quantity = getQuantityFromEditText();
                CartManager.getInstance().addToCart(book, quantity);
                Toast.makeText(this, book.getProductName() + " added to cart. Quantity: " + quantity + "(to be implemented)", Toast.LENGTH_SHORT).show();
                // Close activity after add to cart
                finish();
            });
        } else {
            // Handle book data null
            Toast.makeText(this, "Book data is not found!", Toast.LENGTH_LONG).show();
            finish(); // Close activity
        }
    }

    // Function to get quantity from EditText safely
    private int getQuantityFromEditText() {
        try {
            return Integer.parseInt(itemQuantity.getText().toString());
        } catch (NumberFormatException e) {
            // if error return defaul value: 1
            return 1;
        }
    }
}
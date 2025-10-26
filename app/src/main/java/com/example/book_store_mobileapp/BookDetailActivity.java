package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    private static final String TAG = "BookDetailActivity";

    private ImageView detailBookImage;
    private TextView detailBookName, detailBookAuthor, detailBookDescription, detailBookPrice, detailBookTechnicalSpecifications;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private FirebaseCartService cartService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init views
        detailBookImage = findViewById(R.id.bookImage);
        detailBookName = findViewById(R.id.bookName);
        detailBookAuthor = findViewById(R.id.bookAuthor);
        detailBookDescription = findViewById(R.id.bookDescription);
        detailBookTechnicalSpecifications = findViewById(R.id.bookTechnicalSpecifications);
        detailBookPrice = findViewById(R.id.bookPrice);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBack = findViewById(R.id.btnBack);

        // Service
        cartService = new FirebaseCartService();

        // Lấy dữ liệu sách
        Book book = getIntent().getParcelableExtra("SELECTED_BOOK");

        if (book != null) {
            // Bind dữ liệu
            detailBookName.setText(book.getName());
            detailBookAuthor.setText("Author: " + book.getAuthor());
            detailBookDescription.setText(book.getFullDescription());
            detailBookTechnicalSpecifications.setText(book.getTechnicalSpecifications());

            NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedPrice = format.format(book.getPrice());
            detailBookPrice.setText(formattedPrice + " VNĐ");

            Glide.with(this)
                    .load(book.getImageUrl())
                    .error(android.R.drawable.dark_header)
                    .into(detailBookImage);

            // Thêm vào giỏ
            btnAddToCart.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    // Chưa đăng nhập -> chuyển Login
                    Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    Intent login = new Intent(this, LoginActivity.class);
                    // Có thể muốn quay lại màn này sau khi login:
                    login.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(login);
                    return;
                }

                Log.d(TAG, "Người dùng bấm Thêm vào giỏ hàng: " + book.getName());
                cartService.addToCart(
                        book,
                        1,
                        () -> {
                            Log.d(TAG, "Thêm thành công: " + book.getName());
                            Toast.makeText(BookDetailActivity.this,
                                    book.getName() + " đã được thêm vào giỏ hàng",
                                    Toast.LENGTH_SHORT).show();
                            finish(); // Đóng lại trang chi tiết sau khi thêm
                        },
                        () -> {
                            Log.e(TAG, "Thêm thất bại: " + book.getName());
                            Toast.makeText(BookDetailActivity.this,
                                    "Lỗi khi thêm vào giỏ hàng",
                                    Toast.LENGTH_SHORT).show();
                        }
                );
            });
        } else {
            Toast.makeText(this, "Book data is not found!", Toast.LENGTH_LONG).show();
            finish();
        }

        // Back
        btnBack.setOnClickListener(v -> finish());
    }
}

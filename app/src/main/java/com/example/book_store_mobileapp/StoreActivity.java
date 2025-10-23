package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.book_store_mobileapp.adapter.BookAdapter;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.data.BookFilter;
import com.example.book_store_mobileapp.network.FirebaseBookService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class StoreActivity extends BaseActivity {

    private GridView gridView;
    private ProgressBar progressBar;
    private EditText txtSearchName;
    private ImageButton btnFilter, btnSort;
    private BookAdapter bookAdapter;
    private BookFilter bookFilter;
    private List<Book> initialBookList = new ArrayList<>();
    private List<Book> displayedBookList = new ArrayList<>();
    private String currentSearchQuery = "";
    private int activePriceFilter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Khởi tạo view
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);
        txtSearchName = findViewById(R.id.txtSearchName);
        gridView = findViewById(R.id.grid_view);
        progressBar = findViewById(R.id.progressBar);

//        // ✅ Logout
//        btnLogout.setOnClickListener(v -> {
//            FirebaseAuth.getInstance().signOut();
//            Intent intent = new Intent(StoreActivity.this, LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish();
//        });

//        // ✅ Mở trang Settings (có 2 ô: Tài khoản & Bảo mật, Địa chỉ)
//        btnProfile.setOnClickListener(v -> {
//            startActivity(new Intent(StoreActivity.this, SettingsActivity.class));
//        });

//        // ✅ Giỏ hàng
//        btnCart.setOnClickListener(v -> {
//            Intent intent = new Intent(StoreActivity.this, CartActivity.class);
//            startActivity(intent);
//        });

        // ✅ Bộ lọc & sắp xếp
        bookFilter = new BookFilter();
        txtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFiltersAndSearch();
            }
        });

        btnSort.setOnClickListener(v -> {
            final CharSequence[] options = {"Default", "Price: Low to High", "Price: High to Low"};
            new AlertDialog.Builder(StoreActivity.this)
                    .setTitle("Sort By")
                    .setItems(options, (dialog, item) -> {
                        bookFilter.sortBooksByPrice(displayedBookList, item);
                        bookAdapter.notifyDataSetChanged();
                    })
                    .show();
        });

        btnFilter.setOnClickListener(v -> {
            final CharSequence[] options = {"Under 100,000 VNĐ", "100,000 - 200,000 VNĐ", "Over 200,000 VNĐ", "Clear Filter"};
            new AlertDialog.Builder(StoreActivity.this)
                    .setTitle("Filter by Price Range")
                    .setItems(options, (dialog, item) -> {
                        if (item == 3) activePriceFilter = -1;
                        else activePriceFilter = item;
                        applyFiltersAndSearch();
                    })
                    .show();
        });

        bookAdapter = new BookAdapter(this, displayedBookList);
        gridView.setAdapter(bookAdapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Book selectedBook = displayedBookList.get(position);
            Intent intent = new Intent(StoreActivity.this, BookDetailActivity.class);
            intent.putExtra("SELECTED_BOOK", selectedBook);
            startActivity(intent);
        });

        fetchBooksFromFirebase();
    }

    private void applyFiltersAndSearch() {
        List<Book> filteredList = new ArrayList<>(initialBookList);

        if (activePriceFilter != -1) {
            if (activePriceFilter == 0) { // Under 100,000
                filteredList = bookFilter.filterBooksByPriceRange(filteredList, 0, 99999);
            } else if (activePriceFilter == 1) { // 100,000 - 200,000
                filteredList = bookFilter.filterBooksByPriceRange(filteredList, 100000, 200000);
            } else if (activePriceFilter == 2) { // Over 200,000
                filteredList = bookFilter.filterBooksByPriceRange(filteredList, 200001, Double.MAX_VALUE);
            }
        }

        if (!currentSearchQuery.isEmpty()) {
            filteredList = bookFilter.searchBooks(filteredList, currentSearchQuery);
        }

        updateDisplayedBooks(filteredList);
    }

    private void updateDisplayedBooks(List<Book> bookList) {
        displayedBookList.clear();
        displayedBookList.addAll(bookList);
        bookAdapter.notifyDataSetChanged();
    }

    /**
     * 🟢 Load sách từ Firestore thay cho MockAPI
     */
    private void fetchBooksFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseBookService.getInstance().getAllBooks(new FirebaseBookService.FirestoreCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> data) {
                progressBar.setVisibility(View.GONE);
                initialBookList.clear();
                initialBookList.addAll(data);
                updateDisplayedBooks(initialBookList);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StoreActivity.this, "Lỗi tải sách: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Override phương thức này để cho BaseActivity biết cần highlight mục nào
    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_home;
    }
}

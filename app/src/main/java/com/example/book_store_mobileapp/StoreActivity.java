package com.example.book_store_mobileapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

public class StoreActivity extends AppCompatActivity {

    private GridView gridView;
    private ProgressBar progressBar;
    private EditText txtSearchName;
    private ImageButton btnCart, btnFilter, btnSort, btnLogout;
    private TextView notificationBadge;
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

        // Create notification channel early
        NotificationHelper.createCartChannel(this);
        
        // Add sample notifications for demo
        com.example.book_store_mobileapp.data.NotificationManager.getInstance().addSampleNotifications();

        // ✅ Khởi tạo view
        btnCart = findViewById(R.id.btnCart);
        btnLogout = findViewById(R.id.btnLogout);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        notificationBadge = findViewById(R.id.notificationBadge);
        txtSearchName = findViewById(R.id.txtSearchName);
        gridView = findViewById(R.id.grid_view);
        progressBar = findViewById(R.id.progressBar);

        // ✅ Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(StoreActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // ✅ Giỏ hàng
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(StoreActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // ✅ Nút Notifications
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(StoreActivity.this, NotificationCenterActivity.class);
            startActivity(intent);
        });

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
                        
                        // Add promotion notification when filtering
                        addPromotionNotificationIfNeeded();
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
        
        // Update notification badge
        updateNotificationBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear any cart notifications when returning to the app
        NotificationHelper.cancelCartNotification(this);
        // Update notification badge
        updateNotificationBadge();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Post a cart notification if there are items and permission is granted (Android 13+)
        int totalQty = getCartItemCount();
        if (totalQty > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationHelper.showCartNotification(this, totalQty);
                }
            } else {
                NotificationHelper.showCartNotification(this, totalQty);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Request notification permission on Android 13+ if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
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
                
                // Add new book notifications for demo (only once)
                addNewBookNotificationsIfNeeded();
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StoreActivity.this, "Lỗi tải sách: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNotificationBadge() {
        int unreadCount = com.example.book_store_mobileapp.data.NotificationManager.getInstance().getUnreadCount();
        if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisibility(View.VISIBLE);
        } else {
            notificationBadge.setVisibility(View.GONE);
        }
    }

    private int getCartItemCount() {
        // Get cart item count from Firebase cart service
        // For now, return 0 as we'll implement this properly with Firebase
        return 0;
    }

    private void addNewBookNotificationsIfNeeded() {
        // Add new book notifications for some books (only once)
        com.example.book_store_mobileapp.data.NotificationManager notificationManager = 
            com.example.book_store_mobileapp.data.NotificationManager.getInstance();
        
        // Check if we already added new book notifications
        boolean hasNewBookNotifications = false;
        for (com.example.book_store_mobileapp.data.AppNotification notification : notificationManager.getAllNotifications()) {
            if ("new_book".equals(notification.getType())) {
                hasNewBookNotifications = true;
                break;
            }
        }
        
        if (!hasNewBookNotifications && !initialBookList.isEmpty()) {
            // Add new book notifications for first few books
            for (int i = 0; i < Math.min(3, initialBookList.size()); i++) {
                Book book = initialBookList.get(i);
                notificationManager.addNewBookNotification(book.getName(), book.getAuthor());
            }
        }
    }

    private void addPromotionNotificationIfNeeded() {
        // Add promotion notification only once when user interacts with filters
        com.example.book_store_mobileapp.data.NotificationManager notificationManager = 
            com.example.book_store_mobileapp.data.NotificationManager.getInstance();
        
        // Check if we already added promotion notifications
        boolean hasPromotionNotifications = false;
        for (com.example.book_store_mobileapp.data.AppNotification notification : notificationManager.getAllNotifications()) {
            if ("promotion".equals(notification.getType())) {
                hasPromotionNotifications = true;
                break;
            }
        }
        
        if (!hasPromotionNotifications) {
            notificationManager.addPromotionNotification("Special Filter Offer", "Get 15% off on filtered books!");
        }
    }
}

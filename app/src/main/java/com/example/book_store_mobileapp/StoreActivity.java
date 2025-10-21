package com.example.book_store_mobileapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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
import com.example.book_store_mobileapp.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.firebase.auth.FirebaseAuth;
public class StoreActivity extends AppCompatActivity {

    private GridView gridView;
    private ProgressBar progressBar;
    private EditText txtSearchName;
    private ImageButton btnCart, btnFilter, btnSort, btnLogout; // ✅ thêm btnLogout
    private TextView notificationBadge;
    private BookAdapter bookAdapter;
    private BookFilter bookFilter;
    private List<Book> initialBookList = new ArrayList<>();
    private List<Book> displayedBookList = new ArrayList<>();
    private String currentSearchQuery = "";
    private int activePriceFilter = -1;
    private static final String BASE_URL = "https://68d4d784e29051d1c0ac400e.mockapi.io/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store);

        // Create notification channel early
        NotificationHelper.createCartChannel(this);
        
        // Add sample notifications for demo
        com.example.book_store_mobileapp.data.NotificationManager.getInstance().addSampleNotifications();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Khởi tạo các nút
        btnCart = findViewById(R.id.btnCart);
        btnLogout = findViewById(R.id.btnLogout);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        notificationBadge = findViewById(R.id.notificationBadge);
        txtSearchName = findViewById(R.id.txtSearchName);
        gridView = findViewById(R.id.grid_view);
        progressBar = findViewById(R.id.progressBar);

        // ✅ Nút Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(StoreActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // ✅ Nút Giỏ hàng
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(StoreActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // ✅ Nút Notifications
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(StoreActivity.this, NotificationCenterActivity.class);
            startActivity(intent);
        });

        // --- phần dưới giữ nguyên ---
        bookFilter = new BookFilter();
        txtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // not needed
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //not needed
            }

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

        fetchBooks();
        
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
        int totalQty = com.example.book_store_mobileapp.data.CartManager.getInstance().getTotalQuantity();
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
        // 1. Start with the full list
        List<Book> filteredList = new ArrayList<>(initialBookList);
        // 2. Apply the price filter first (if one is active)
        if (activePriceFilter != -1) {
            if (activePriceFilter == 0) { // Under 100,000
                filteredList = bookFilter.filterBooksByPriceRange(filteredList, 0, 99999);
            } else if (activePriceFilter == 1) { // 100,000 - 200,000
                filteredList = bookFilter.filterBooksByPriceRange(filteredList, 100000, 200000);
            } else if (activePriceFilter == 2) { // Over 200,000
                filteredList = bookFilter.filterBooksByPriceRange(filteredList, 200001, Double.MAX_VALUE);
            }
        }
        // 3. Then, apply the search query on the result of the price filter
        if (!currentSearchQuery.isEmpty()) {
            filteredList = bookFilter.searchBooks(filteredList, currentSearchQuery);
        }
        // 4. Finally, update the UI
        updateDisplayedBooks(filteredList);
    }
    private void updateDisplayedBooks(List<Book> bookList){
        //Clear old displayed list and add new list
        displayedBookList.clear();
        displayedBookList.addAll(bookList);
        //Notify the adapter that the data has changed
        bookAdapter.notifyDataSetChanged();
    }

    // Get book from API
    private void fetchBooks(){
        progressBar.setVisibility(View.VISIBLE); //Show loading

        // 1. Initialize retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // 2. Create API service instance
        ApiService apiService = retrofit.create(ApiService.class);
        // 3. Make API call
        Call<List<Book>> call = apiService.getBooks();
        call.enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                progressBar.setVisibility(View.GONE);
                if(response.isSuccessful() && response.body() != null){
                    //Clear old initial list and add new list
                    initialBookList.clear();
                    initialBookList.addAll(response.body());
                    //Default displayed books
                    updateDisplayedBooks(initialBookList);
                    
                    // Add new book notifications for demo (only once)
                    addNewBookNotificationsIfNeeded();
                } else {
                    Toast.makeText(StoreActivity.this, "Fail to retrive books", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StoreActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
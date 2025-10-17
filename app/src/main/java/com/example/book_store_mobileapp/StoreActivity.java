package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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
import com.example.book_store_mobileapp.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StoreActivity extends AppCompatActivity {

    private GridView gridView;
    private ProgressBar progressBar;
    private EditText txtSearchName;
    private ImageButton btnCart, btnFilter, btnSort;
    private BookAdapter bookAdapter;
    private BookFilter bookFilter;
    private List<Book> initialBookList = new ArrayList<>();
    private List<Book> displayedBookList = new ArrayList<>();
    private String currentSearchQuery = ""; // 1. To store the current search text
    private int activePriceFilter = -1;
    private static final String BASE_URL = "https://68d4d784e29051d1c0ac400e.mockapi.io/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) ->{
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnCart = findViewById(R.id.btnCart);
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(StoreActivity.this, CartActivity.class);
            startActivity(intent);
        });


        // Initialize views
        gridView = findViewById(R.id.grid_view);
        progressBar = findViewById(R.id.progressBar);
        txtSearchName = findViewById(R.id.txtSearchName);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);

        // Call Book filter
        bookFilter = new BookFilter();
        // Search
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
//                List<Book> filteredBooks = bookFilter.searchBooks(initialBookList, s.toString());
//                updateDisplayedBooks(filteredBooks);
                currentSearchQuery = s.toString();
                applyFiltersAndSearch();
            }
        });
        // Sort
        btnSort.setOnClickListener(v -> {
            final CharSequence[] options = {"Default", "Price: Low to High", "Price: High to Low"};
            new AlertDialog.Builder(StoreActivity.this)
                    .setTitle("Sort By")
                    .setItems(options, (dialog, item) -> {
                        bookFilter.sortBooksByPrice(displayedBookList, item);
                        bookAdapter.notifyDataSetChanged(); // Refresh the grid view
                    })
                    .show();
        });
        // Filter
        btnFilter.setOnClickListener(v -> {
            final CharSequence[] options = {"Under 100,000 VNĐ", "100,000 - 200,000 VNĐ", "Over 200,000 VNĐ", "Clear Filter"};
            new AlertDialog.Builder(StoreActivity.this)
                    .setTitle("Filter by Price Range")
                    .setItems(options, (dialog, item) -> {
                        if (item == 3) { // "Clear Filter"
                            activePriceFilter = -1;
                        } else {
                            activePriceFilter = item; // Update the price filter state
                        }
                        applyFiltersAndSearch();
                    })
                    .show();
        });

        // Initialize book adapter with an empty list
        bookAdapter = new BookAdapter(this, displayedBookList);
        gridView.setAdapter(bookAdapter);

        // Item click listener to open book detail
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            // 1. Get chosen book
            Book selectedBook = displayedBookList.get(position);
            // 2. Create Intent to open BookDetailActivity
            Intent intent = new Intent(StoreActivity.this, BookDetailActivity.class);
            // 3. Pass selected book to activity
            intent.putExtra("SELECTED_BOOK", selectedBook);
            // 4. Start activity
            startActivity(intent);
        });

        fetchBooks();
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

}
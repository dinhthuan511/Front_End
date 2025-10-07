package com.example.book_store_mobileapp;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.book_store_mobileapp.adapter.BookAdapter;
import com.example.book_store_mobileapp.data.Book;
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
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();
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

        gridView = findViewById(R.id.grid_view);
        progressBar = findViewById(R.id.progressBar);

        // Initialize book adapter with an empty list
        bookAdapter = new BookAdapter(this, bookList);
        gridView.setAdapter(bookAdapter);

        fetchBooks();
    }

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
                    //Clear old list and add new list
                    bookList.clear();
                    bookList.addAll(response.body());
                    //Notify the adapter that the data has changed
                    bookAdapter.notifyDataSetChanged();
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
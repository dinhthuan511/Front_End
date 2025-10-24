package com.example.book_store_mobileapp.network;

import com.example.book_store_mobileapp.data.Book;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    //Replace later with real API
    @GET("products")
    Call<List<Book>> getBooks();
}

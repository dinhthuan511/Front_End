package com.example.book_store_mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.data.Book;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends ArrayAdapter<Book> {
    public BookAdapter(Context context, List<Book> books){
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse an existing view if one is available (the convertView).
        // If not, inflate a new view from our custom layout file (grid_item_book.xml).
        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_book, parent, false);
        }

        // Get book for the current position
        Book currentBook = getItem(position);

        // Initialize views
        ImageView bookImage = listItemView.findViewById(R.id.book_image);
        TextView bookName = listItemView.findViewById(R.id.book_name);
        TextView bookPrice = listItemView.findViewById(R.id.book_price);

        // Set book data
        bookName.setText(currentBook.getName());
        // Format the price
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedPrice = format.format(currentBook.getPrice());
        bookPrice.setText(formattedPrice + " VNĐ");
        // Set image with Glide
        Glide.with(getContext())
                .load(currentBook.getImageUrl())
                .placeholder(android.R.drawable.dark_header)
                .error(android.R.drawable.dark_header)
                .into(bookImage);

        return listItemView;
    }
}

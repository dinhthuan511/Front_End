package com.example.book_store_mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.book_store_mobileapp.FormatUtils;
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
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_book, parent, false);
        }

        // Get book for the current position
        Book currentBook = getItem(position);

        // Initialize views
        ImageView bookImage = listItemView.findViewById(R.id.book_image);
        TextView bookName = listItemView.findViewById(R.id.book_name);
        TextView bookPrice = listItemView.findViewById(R.id.book_price);
        TextView overlayOutOfStock = listItemView.findViewById(R.id.overlay_out_of_stock);
        LinearLayout mainContent = listItemView.findViewById(R.id.main_content);

        if (currentBook != null) {
            // Set book data
            bookName.setText(currentBook.getName());
            // Format the price
            bookPrice.setText(FormatUtils.formatCurrency(currentBook.getPrice()));
            // Set image with Glide
            if (currentBook.getImageBase64() != null && !currentBook.getImageBase64().isEmpty()) {
                Glide.with(getContext())
                        .load(currentBook.getImageBase64().get(0)) // ✅ chỉ lấy ảnh đầu tiên
                        .placeholder(android.R.drawable.dark_header)
                        .error(android.R.drawable.dark_header)
                        .into(bookImage);
            } else {
                bookImage.setImageResource(android.R.drawable.dark_header);
            }

            // Check if the book is out of stock
            if (currentBook.getStock() != null && currentBook.getStock() <= 0) {
                overlayOutOfStock.setVisibility(View.VISIBLE);
                mainContent.setAlpha(0.25f); // blur the main content
            } else {
                overlayOutOfStock.setVisibility(View.GONE);
                mainContent.setAlpha(1.0f); // unblur the main content
            }
        }
        return listItemView;
    }
}

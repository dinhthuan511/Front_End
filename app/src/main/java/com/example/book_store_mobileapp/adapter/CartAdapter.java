package com.example.book_store_mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.data.CartItem;
import com.example.book_store_mobileapp.data.CartManager;
import java.util.List;

public class CartAdapter extends BaseAdapter {

    private Context context;
    private List<CartItem> cartItems;
    private Runnable onCartUpdated;

    public CartAdapter(Context context, List<CartItem> cartItems, Runnable onCartUpdated) {
        this.context = context;
        this.cartItems = cartItems;
        this.onCartUpdated = onCartUpdated;
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public Object getItem(int i) {
        return cartItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);

        CartItem item = cartItems.get(i);
        Book book = item.getBook();

        ImageView imgBook = convertView.findViewById(R.id.imgBook);
        TextView txtBookName = convertView.findViewById(R.id.txtBookName);
        TextView txtAuthor = convertView.findViewById(R.id.txtAuthor);
        TextView txtPrice = convertView.findViewById(R.id.txtPrice);
        TextView txtQuantity = convertView.findViewById(R.id.txtQuantity);
        Button btnPlus = convertView.findViewById(R.id.btnPlus);
        Button btnMinus = convertView.findViewById(R.id.btnMinus);
        ImageButton btnRemove = convertView.findViewById(R.id.btnRemove);

        txtBookName.setText(book.getName());
        txtAuthor.setText("by " + book.getAuthor());
        txtPrice.setText("Total: $" + String.format("%.2f", item.getTotalPrice()));
        txtQuantity.setText(String.valueOf(item.getQuantity()));
        Glide.with(context).load(book.getImageUrl()).into(imgBook);

        btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            onCartUpdated.run();
            notifyDataSetChanged();
        });

        btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                onCartUpdated.run();
                notifyDataSetChanged();
            }
        });

        btnRemove.setOnClickListener(v -> {
            CartManager.getInstance().removeFromCart(book.getBookId());
            onCartUpdated.run();
            notifyDataSetChanged();
        });

        return convertView;
    }
}


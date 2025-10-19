package com.example.book_store_mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.network.FirebaseCartService;

import java.util.ArrayList;

public class CartAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Book> items;
    private final Runnable onUpdateTotal;
    private final FirebaseCartService cartService;

    public CartAdapter(Context context, ArrayList<Book> items, Runnable onUpdateTotal) {
        this.context = context;
        this.items = items;
        this.onUpdateTotal = onUpdateTotal;
        this.cartService = new FirebaseCartService();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        }

        Book book = items.get(position);

        // Ánh xạ view
        ImageView imgBook = convertView.findViewById(R.id.imgBook);
        TextView txtBookName = convertView.findViewById(R.id.txtBookName);
        TextView txtBookPrice = convertView.findViewById(R.id.txtBookPrice);
        TextView txtQuantity = convertView.findViewById(R.id.txtQuantity);
        Button btnMinus = convertView.findViewById(R.id.btnDecrease);
        Button btnPlus = convertView.findViewById(R.id.btnIncrease);
        Button btnRemove = convertView.findViewById(R.id.btnRemove);

        // Gán dữ liệu
        txtBookName.setText(book.getName());
        txtBookPrice.setText("$" + String.format("%.2f", book.getPrice()));
        Glide.with(context).load(book.getImageUrl()).into(imgBook);

        // ⚠️ Firestore chưa có field "quantity" trong Book, bạn có thể thêm tạm vào item metadata
        // nên tạm giả định bạn đang lấy quantity từ Firestore (gán mặc định 1 nếu chưa có)
        int[] quantity = {1};
        txtQuantity.setText(String.valueOf(quantity[0]));

        // Nút tăng
        btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            txtQuantity.setText(String.valueOf(quantity[0]));
            cartService.updateQuantity(book.getBookId(), quantity[0],
                    () -> Toast.makeText(context, "Cập nhật số lượng +1", Toast.LENGTH_SHORT).show(),
                    () -> Toast.makeText(context, "Lỗi cập nhật", Toast.LENGTH_SHORT).show());
            onUpdateTotal.run();
        });

        // Nút giảm
        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                txtQuantity.setText(String.valueOf(quantity[0]));
                cartService.updateQuantity(book.getBookId(), quantity[0],
                        () -> Toast.makeText(context, "Cập nhật số lượng -1", Toast.LENGTH_SHORT).show(),
                        () -> Toast.makeText(context, "Lỗi cập nhật", Toast.LENGTH_SHORT).show());
                onUpdateTotal.run();
            }
        });

        // Nút xóa
        btnRemove.setOnClickListener(v -> {
            cartService.removeFromCart(book.getBookId(),
                    () -> {
                        items.remove(position);
                        notifyDataSetChanged();
                        onUpdateTotal.run();
                        Toast.makeText(context, "Đã xóa sản phẩm khỏi giỏ", Toast.LENGTH_SHORT).show();
                    },
                    () -> Toast.makeText(context, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show());
        });

        return convertView;
    }
}

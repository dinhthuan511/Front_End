package com.example.book_store_mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.data.CartItem;
import com.example.book_store_mobileapp.network.FirebaseCartService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<CartItem> items;
    private final Runnable onUpdateTotal;
    private final FirebaseCartService cartService;

    public CartAdapter(Context context, ArrayList<CartItem> items, Runnable onUpdateTotal) {
        this.context = context;
        this.items = items;
        this.onUpdateTotal = onUpdateTotal;
        this.cartService = new FirebaseCartService();
    }

    @Override
    public int getCount() { return items.size(); }

    @Override
    public Object getItem(int position) { return items.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);

        CartItem item = items.get(position);
        Book book = item.getBook();
        int quantity = item.getQuantity();

        ImageView imgBook = convertView.findViewById(R.id.imgBook);
        TextView txtBookName = convertView.findViewById(R.id.txtBookName);
        TextView txtBookPrice = convertView.findViewById(R.id.txtBookPrice);
        TextView txtQuantity = convertView.findViewById(R.id.txtQuantity);
        Button btnMinus = convertView.findViewById(R.id.btnDecrease);
        Button btnPlus = convertView.findViewById(R.id.btnIncrease);
        ImageButton btnRemove = convertView.findViewById(R.id.btnRemove);

        txtBookName.setText(book.getName());

        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedPrice = format.format(book.getPrice());
        txtBookPrice.setText(formattedPrice + " VNĐ");

        txtQuantity.setText(String.valueOf(quantity));
        Glide.with(context).load(book.getImageUrl()).into(imgBook);

        // Nút tăng
        btnPlus.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            txtQuantity.setText(String.valueOf(newQuantity));

            cartService.updateQuantity(item.getCartId(), newQuantity,
                    () -> Toast.makeText(context, "Cập nhật số lượng +1", Toast.LENGTH_SHORT).show(),
                    () -> Toast.makeText(context, "Lỗi cập nhật", Toast.LENGTH_SHORT).show());
            onUpdateTotal.run();
        });

        // Nút giảm
        btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQuantity = item.getQuantity() - 1;
                item.setQuantity(newQuantity);
                txtQuantity.setText(String.valueOf(newQuantity));

                cartService.updateQuantity(item.getCartId(), newQuantity,
                        () -> Toast.makeText(context, "Cập nhật số lượng -1", Toast.LENGTH_SHORT).show(),
                        () -> Toast.makeText(context, "Lỗi cập nhật", Toast.LENGTH_SHORT).show());
                onUpdateTotal.run();
            }
        });

        btnRemove.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // ✅ Lấy cartItemId (document ID trong Firestore)
                        String cartItemId = item.getCartId();

                        if (cartItemId == null || cartItemId.isEmpty()) {
                            Toast.makeText(context, "Không tìm thấy ID sản phẩm trong giỏ!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // ✅ Gọi hàm xóa theo cartItemId
                        cartService.removeCartItemById(cartItemId,
                                () -> {
                                    // Xóa trong danh sách hiển thị
                                    items.remove(position);
                                    notifyDataSetChanged();
                                    onUpdateTotal.run();

                                    Toast.makeText(context, "Đã xóa sản phẩm khỏi giỏ", Toast.LENGTH_SHORT).show();
                                },
                                () -> Toast.makeText(context, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });

        return convertView;

    }
}

package com.example.book_store_mobileapp.ui.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.book_store_mobileapp.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.VH> {

    public interface OnProductAction {
        void onEdit(DocumentSnapshot doc);
        void onDelete(DocumentSnapshot doc);
    }

    private final List<DocumentSnapshot> items;
    private final OnProductAction action;

    public AdminProductAdapter(List<DocumentSnapshot> items, OnProductAction action) {
        this.items = items;
        this.action = action;
        setHasStableIds(true); // giúp RecyclerView mượt hơn khi cập nhật
    }

    @Override public long getItemId(int position) {
        // Dùng id tài liệu làm stable id nếu có
        return items.get(position).getId().hashCode();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        DocumentSnapshot d = items.get(pos);

        // 🔁 ĐỔI FIELD THEO SCHEMA MỚI
        String title = d.getString("productName"); // trước đây là "name"
        String img   = d.getString("imageURL");    // trước đây là "imageUrl"
        Long price   = asLong(d.get("price"));     // có thể là Long/Double -> ép về Long

        h.tvName.setText(title != null ? title : "(no name)");

        if (price != null) {
            NumberFormat f = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            h.tvPrice.setText(f.format(price) + " đ");
        } else {
            h.tvPrice.setText("-");
        }

        Glide.with(h.iv.getContext())
                .load(img)
                .placeholder(android.R.color.darker_gray)
                .into(h.iv);

        h.btnEdit.setOnClickListener(v -> action.onEdit(d));
        h.btnDelete.setOnClickListener(v -> action.onDelete(d));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tvName, tvPrice;
        ImageButton btnEdit, btnDelete;
        VH(@NonNull View v) {
            super(v);
            iv       = v.findViewById(R.id.iv);
            tvName   = v.findViewById(R.id.tvName);
            tvPrice  = v.findViewById(R.id.tvPrice);
            btnEdit  = v.findViewById(R.id.btnEdit);
            btnDelete= v.findViewById(R.id.btnDelete);
        }
    }

    /** Ép mọi kiểu số (Long/Double/Integer) về Long an toàn */
    private static Long asLong(Object o) {
        if (o instanceof Number) return ((Number) o).longValue();
        return null;
    }
}
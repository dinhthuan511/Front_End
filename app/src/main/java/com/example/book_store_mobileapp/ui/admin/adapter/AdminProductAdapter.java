package com.example.book_store_mobileapp.ui.admin.adapter;

import android.text.TextUtils;
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

    @Override
    public long getItemId(int position) {
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

        String title = d.getString("productName");
        Long price   = asLong(d.get("price"));

        h.tvName.setText(!TextUtils.isEmpty(title) ? title : "(no name)");
        if (price != null && price > 0) {
            NumberFormat f = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            h.tvPrice.setText(f.format(price) + " đ");
        } else {
            h.tvPrice.setText("-");
        }

        // ✅ Ưu tiên ảnh base64; fallback ảnh URL
        String b64 = d.getString("imageBase64");
        String url = d.getString("imageURL");

        if (!TextUtils.isEmpty(b64)) {
            String dataUrl = "data:image/jpeg;base64," + b64;
            Glide.with(h.iv.getContext())
                    .load(dataUrl)
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.darker_gray)
                    .into(h.iv);
        } else if (!TextUtils.isEmpty(url)) {
            Glide.with(h.iv.getContext())
                    .load(url)
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.darker_gray)
                    .into(h.iv);
        } else {
            h.iv.setImageResource(android.R.color.darker_gray);
        }

        h.btnEdit.setOnClickListener(v -> {
            if (action != null) action.onEdit(d);
        });
        h.btnDelete.setOnClickListener(v -> {
            if (action != null) action.onDelete(d);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tvName, tvPrice;
        ImageButton btnEdit, btnDelete;
        VH(@NonNull View v) {
            super(v);
            iv        = v.findViewById(R.id.iv);          // giữ nguyên id layout của bạn
            tvName    = v.findViewById(R.id.tvName);
            tvPrice   = v.findViewById(R.id.tvPrice);
            btnEdit   = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

    /** Ép mọi kiểu số (Long/Double/Integer) về Long an toàn */
    private static Long asLong(Object o) {
        if (o instanceof Number) return ((Number) o).longValue();
        return null;
    }
}

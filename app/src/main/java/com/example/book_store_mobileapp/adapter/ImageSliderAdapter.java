package com.example.book_store_mobileapp.ui.components;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.R;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private final List<String> imageBase64List;

    public ImageSliderAdapter(List<String> imageBase64List) {
        this.imageBase64List = imageBase64List;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slider, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String base64 = imageBase64List.get(position);
        if (base64 != null && !base64.isEmpty()) {
            // Nếu có prefix, bỏ đi
            if (base64.startsWith("data:image")) {
                int commaIndex = base64.indexOf(',');
                if (commaIndex != -1) {
                    base64 = base64.substring(commaIndex + 1);
                }
            }

            try {
                byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if(decodedByte != null){
                    holder.imageView.setImageBitmap(decodedByte);
                } else {
                    holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            }

        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }


    @Override
    public int getItemCount() {
        return imageBase64List != null ? imageBase64List.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image); // ✅ sửa id
        }
    }

}

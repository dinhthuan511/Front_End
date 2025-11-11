package com.example.book_store_mobileapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private final List<Uri> images;
    private final Context context;

    public ImagesAdapter(Context ctx, List<Uri> images) {
        this.context = ctx;
        this.images = images;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(context);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(200, 200);
        params.setMargins(8,8,8,8);
        iv.setLayoutParams(params);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ViewHolder(iv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(context).load(images.get(position)).into(holder.imageView);
        holder.imageView.setOnClickListener(v -> {
            images.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() { return images.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ViewHolder(ImageView iv) { super(iv); imageView = iv; }
    }
}

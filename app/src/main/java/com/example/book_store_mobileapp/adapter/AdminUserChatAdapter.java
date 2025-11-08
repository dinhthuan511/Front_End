package com.example.book_store_mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.data.UserChatInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminUserChatAdapter extends RecyclerView.Adapter<AdminUserChatAdapter.ViewHolder> {

    private Context context;
    private List<UserChatInfo> userChatList;
    private OnUserChatClickListener listener;

    public interface OnUserChatClickListener {
        void onUserChatClick(UserChatInfo userChatInfo);
    }

    public AdminUserChatAdapter(Context context, List<UserChatInfo> userChatList, OnUserChatClickListener listener) {
        this.context = context;
        this.userChatList = userChatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserChatInfo chatInfo = userChatList.get(position);

        holder.tvUserEmail.setText(chatInfo.getUserEmail());
        holder.tvLastMessage.setText(chatInfo.getLastMessage());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(chatInfo.getLastMessageTime()));
        holder.tvTimestamp.setText(formattedTime);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserChatClick(chatInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userChatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvLastMessage, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
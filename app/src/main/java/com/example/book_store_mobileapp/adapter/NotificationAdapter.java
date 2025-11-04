package com.example.book_store_mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.data.AppNotification;

import java.util.Date;
import java.util.List;

public class NotificationAdapter extends BaseAdapter {
    private Context context;
    private List<AppNotification> notifications;
    private LayoutInflater inflater;

    public NotificationAdapter(Context context, List<AppNotification> notifications) {
        this.context = context;
        this.notifications = notifications;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_notification, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.notificationIcon);
            holder.title = convertView.findViewById(R.id.txtTitle);
            holder.message = convertView.findViewById(R.id.txtMessage);
            holder.timestamp = convertView.findViewById(R.id.txtTimestamp);
            holder.unreadIndicator = convertView.findViewById(R.id.unreadIndicator);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppNotification notification = notifications.get(position);

        // Set notification content
        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());

        // Set timestamp
        String timeAgo = getTimeAgo(notification.getTimestamp());
        holder.timestamp.setText(timeAgo);

        // Set icon based on notification type
        int iconRes = R.drawable.ic_notifications_24;
        switch (notification.getType()) {
            case "purchase":
                iconRes = R.drawable.ic_add_shopping_cart_black_24dp;
                break;
            case "new_book":
                iconRes = R.drawable.ic_launcher_foreground;
                break;
            case "promotion":
                iconRes = R.drawable.filter_alt_24px;
                break;
        }
        holder.icon.setImageResource(iconRes);

        // Show/hide unread indicator
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

        return convertView;
    }

    private String getTimeAgo(Date timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp.getTime();

        long minutes = diff / (1000 * 60);
        long hours = diff / (1000 * 60 * 60);
        long days = diff / (1000 * 60 * 60 * 24);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView message;
        TextView timestamp;
        View unreadIndicator;
    }
}


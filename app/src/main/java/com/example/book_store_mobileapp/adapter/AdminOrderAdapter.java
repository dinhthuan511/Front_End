package com.example.book_store_mobileapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.data.Order;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnStatusChangeListener listener;

    public interface OnStatusChangeListener {
        void onStatusChange(Order order, String newStatus);
        void onCancelOrder(Order order, String reason); // ✅ thêm lý do
    }

    public AdminOrderAdapter(Context context, List<Order> orderList, OnStatusChangeListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Mã đơn: " + order.getOrderId());
        holder.tvUserName.setText("Người mua: " + order.getName());
        holder.tvTotal.setText("Tổng tiền: " + order.getTotal() + " ₫");
        holder.tvCreatedAt.setText("Ngày tạo: " + formatDate(order.getCreatedAt()));

        // Danh sách trạng thái
        String[] statusOptions = {"Đang xử lý", "Đang giao", "Đã giao"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerStatus.setAdapter(spinnerAdapter);

        int selectedPos = spinnerAdapter.getPosition(order.getStatus());
        if (selectedPos >= 0) holder.spinnerStatus.setSelection(selectedPos, false);

        holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstCall = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (firstCall) {
                    firstCall = false;
                    return;
                }

                String newStatus = parent.getItemAtPosition(pos).toString();
                String currentStatus = order.getStatus();

                if (currentStatus.equals("Đang giao") && newStatus.equals("Đang xử lý")) {
                    Toast.makeText(context, "Không thể quay lại 'Đang xử lý'", Toast.LENGTH_SHORT).show();
                    holder.spinnerStatus.setSelection(getStatusPosition(currentStatus));
                    return;
                }
                if (currentStatus.equals("Đã giao") &&
                        (newStatus.equals("Đang giao") || newStatus.equals("Đang xử lý"))) {
                    Toast.makeText(context, "Đơn đã hoàn tất, không thể thay đổi!", Toast.LENGTH_SHORT).show();
                    holder.spinnerStatus.setSelection(getStatusPosition(currentStatus));
                    return;
                }

                if (!newStatus.equals(currentStatus)) {
                    listener.onStatusChange(order, newStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Ẩn nút Hủy nếu đơn không thể hủy
        if (order.getStatus().equals("Đang giao") || order.getStatus().equals("Đã giao") || order.getStatus().equals("Đã hủy")) {
            holder.btnCancel.setVisibility(View.GONE);
        } else {
            holder.btnCancel.setVisibility(View.VISIBLE);
        }

        holder.btnCancel.setOnClickListener(v -> {

            if (order.getStatus().equals("Đang giao") || order.getStatus().equals("Đã giao")) {
                Toast.makeText(context, "Không thể hủy đơn đang vận chuyển!", Toast.LENGTH_SHORT).show();
                return;
            }

            showCancelReasonDialog(order);
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvUserName, tvTotal, tvCreatedAt;
        Spinner spinnerStatus;
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }

    private void showCancelReasonDialog(Order order) {
        EditText input = new EditText(context);
        input.setHint("Nhập lý do hủy đơn...");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(context)
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Vui lòng nhập lý do hủy đơn:")
                .setView(input)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(context, "Lý do không được để trống!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listener.onCancelOrder(order, reason);
                })
                .setNegativeButton("Thoát", null)
                .show();
    }

    private int getStatusPosition(String status) {
        switch (status) {
            case "Đang xử lý": return 0;
            case "Đang giao": return 1;
            case "Đã giao": return 2;
            default: return 0;
        }
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "Không có dữ liệu";
        Date date = timestamp.toDate();
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
    }
}

package com.example.book_store_mobileapp.ui.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.adapter.AdminOrderAdapter;
import com.example.book_store_mobileapp.data.Order;
import com.example.book_store_mobileapp.network.FirebaseOrderService;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private AdminOrderAdapter adapter;
    private FirebaseOrderService orderService;
    private List<Order> orderList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        recyclerOrders = findViewById(R.id.recyclerOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));

        orderService = new FirebaseOrderService();
        loadOrders();
    }

    private void loadOrders() {
        orderService.getAllOrders(orders -> {
            orderList.clear();
            orderList.addAll(orders);

            adapter = new AdminOrderAdapter(
                    AdminOrdersActivity.this,
                    orderList,
                    new AdminOrderAdapter.OnStatusChangeListener() {

                        @Override
                        public void onStatusChange(Order order, String newStatus) {
                            orderService.updateOrderStatus(
                                    order.getOrderId(),
                                    newStatus,
                                    () -> {
                                        order.setStatus(newStatus);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(AdminOrdersActivity.this,
                                                "Cập nhật trạng thái thành công!",
                                                Toast.LENGTH_SHORT).show();
                                    },
                                    e -> Toast.makeText(AdminOrdersActivity.this,
                                            "Lỗi cập nhật: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onCancelOrder(Order order, String reason) {
                            orderService.updateOrderStatusWithReason(
                                    order.getOrderId(),
                                    "Đã hủy",
                                    reason,
                                    () -> {
                                        order.setStatus("Đã hủy");
                                        adapter.notifyDataSetChanged();

                                        // Animation mờ dần
                                        int pos = orderList.indexOf(order);
                                        if (pos >= 0) {
                                            RecyclerView.ViewHolder vh = recyclerOrders.findViewHolderForAdapterPosition(pos);
                                            if (vh != null) {
                                                vh.itemView.animate().alpha(0.4f).setDuration(500).start();
                                            }
                                        }

                                        Toast.makeText(AdminOrdersActivity.this,
                                                "Đã hủy đơn hàng thành công!",
                                                Toast.LENGTH_SHORT).show();
                                    },
                                    e -> Toast.makeText(AdminOrdersActivity.this,
                                            "Lỗi hủy đơn: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    });

            recyclerOrders.setAdapter(adapter);

        }, e -> Toast.makeText(this,
                "Lỗi tải đơn hàng: " + e.getMessage(),
                Toast.LENGTH_SHORT).show());
    }
}

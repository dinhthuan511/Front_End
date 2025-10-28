package com.example.book_store_mobileapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.adapter.AdminOrderAdapter;
import com.example.book_store_mobileapp.data.Order;
import com.example.book_store_mobileapp.network.FirebaseOrderService;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private AdminOrderAdapter adapter;
    private FirebaseOrderService orderService;
    private List<Order> orderList = new ArrayList<>();
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        recyclerOrders = findViewById(R.id.recyclerOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));

        btnBack = findViewById(R.id.btnBack); // ✅ nút back trong layout
        btnBack.setOnClickListener(v -> finish());

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

                        // ✅ Khi admin thay đổi trạng thái
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

                        // ✅ Khi admin chọn "Hủy đơn hàng"
                        @Override
                        public void onCancelOrder(Order order) {
                            orderService.updateOrderStatus(
                                    order.getOrderId(),
                                    "Đã hủy",
                                    () -> {
                                        order.setStatus("Đã hủy");
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(AdminOrdersActivity.this,
                                                "Đơn hàng đã bị hủy!",
                                                Toast.LENGTH_SHORT).show();
                                    },
                                    e -> Toast.makeText(AdminOrdersActivity.this,
                                            "Lỗi hủy đơn: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    });

            recyclerOrders.setAdapter(adapter);
        }, e -> Toast.makeText(this, "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

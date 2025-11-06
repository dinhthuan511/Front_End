package com.example.book_store_mobileapp.network;

import com.example.book_store_mobileapp.data.Order;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FirebaseOrderService {
    private final FirebaseFirestore db;
    private final CollectionReference ordersRef;

    public FirebaseOrderService() {
        db = FirebaseFirestore.getInstance();
        ordersRef = db.collection("orders");
    }

    // 🔹 Lấy tất cả đơn hàng
    @SuppressWarnings("unchecked")
    public void getAllOrders(Consumer<List<Order>> onSuccess, Consumer<Exception> onError) {
        ordersRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Order> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            Order order = doc.toObject(Order.class);
                            order.setOrderId(doc.getId());
                            list.add(order);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    onSuccess.accept(list);
                })
                .addOnFailureListener(onError::accept);
    }

    // 🔹 Cập nhật trạng thái đơn hàng
    public void updateOrderStatus(String orderId, String newStatus, Runnable onSuccess, Consumer<Exception> onError) {
        ordersRef.document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(unused -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }

    // 🔹 Cập nhật hủy đơn (lưu lý do + thời gian)
    public void updateOrderStatusWithReason(String orderId, String status, String reason,
                                            Runnable onSuccess, Consumer<Exception> onError) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("cancelReason", reason);
        updates.put("cancelledAt", Timestamp.now());

        ordersRef.document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }
}

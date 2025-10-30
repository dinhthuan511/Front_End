package com.example.book_store_mobileapp.network;

import com.example.book_store_mobileapp.data.Order;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
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
}

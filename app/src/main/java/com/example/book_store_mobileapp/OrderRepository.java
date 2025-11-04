package com.example.book_store_mobileapp;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.book_store_mobileapp.data.CartItem;
import com.example.book_store_mobileapp.network.FirebaseCartService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {

    public static void saveOrder(Context context,
                                 ArrayList<CartItem> cartItems,
                                 double total,
                                 String name,
                                 String phone,
                                 String address,
                                 String paymentMethod,
                                 String paymentStatus,
                                 String zpTransId,   // may be null
                                 String zpOrderId,   // may be null
                                 String zpToken) {   // may be null

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Map<String, Object> order = new HashMap<>();
        if (user != null) order.put("userId", user.getUid());
        order.put("name", name);
        order.put("phone", phone);
        order.put("address", address);
        order.put("total", total);
        order.put("paymentMethod", paymentMethod);
        order.put("paymentStatus", paymentStatus);
        order.put("status", "Chờ xác nhận");
        order.put("createdAt", FieldValue.serverTimestamp());

        // add zalo transaction info if provided
        if (zpTransId != null) order.put("zpTransId", zpTransId);
        if (zpOrderId != null) order.put("zpOrderId", zpOrderId);
        if (zpToken != null) order.put("zpToken", zpToken);

        // items
        List<Map<String, Object>> itemsList = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("quantity", item.getQuantity());

                Map<String, Object> bookMap = new HashMap<>();
                bookMap.put("bookId", item.getBook().getBookId());
                // adapt your Book getters (name/title)
                bookMap.put("name", item.getBook().getName());
                bookMap.put("price", item.getBook().getPrice());
                bookMap.put("imageUrl", item.getBook().getImageUrl());

                itemMap.put("book", bookMap);
                itemsList.add(itemMap);
            }
        }
        order.put("items", itemsList);

        db.collection("orders").add(order)
                .addOnSuccessListener(docRef -> {
                    // clear cart if user logged in
                    if (user != null) {
                        new FirebaseCartService().clearCart(user.getUid(), () -> {
                            // open PaymentSuccessActivity
                            Intent intent = new Intent(context, PaymentSuccessActivity.class);
                            intent.putExtra("orderId", docRef.getId());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });
                    } else {
                        Toast.makeText(context, "Order saved but user not logged in", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lưu đơn hàng thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

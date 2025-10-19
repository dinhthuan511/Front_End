package com.example.book_store_mobileapp.network;

import com.example.book_store_mobileapp.data.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class FirebaseCartService {

    private final FirebaseFirestore db;
    private final String userId;

    public FirebaseCartService() {
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    // 🟢 Cho phép class khác (như CartActivity) truy cập
    public CollectionReference getCartRef() {
        return db.collection("users").document(userId).collection("cart");
    }

    /**
     * 🟢 Thêm sản phẩm vào giỏ hàng trên Firestore
     */
    public void addToCart(Book book, int quantity, Runnable onSuccess, Runnable onFailure) {
        if (userId == null) {
            if (onFailure != null) onFailure.run();
            return;
        }

        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("bookId", book.getBookId());
        cartItem.put("productName", book.getName());
        cartItem.put("author", book.getAuthor());
        cartItem.put("description", book.getDescription());
        cartItem.put("imageURL", book.getImageUrl());
        cartItem.put("price", book.getPrice());
        cartItem.put("quantity", quantity);

        getCartRef().document(book.getBookId())
                .set(cartItem)
                .addOnSuccessListener(unused -> {
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.run();
                });
    }

    /**
     * 🔴 Xóa sản phẩm khỏi giỏ hàng
     */
    public void removeFromCart(String bookId, Runnable onSuccess, Runnable onFailure) {
        if (userId == null) {
            if (onFailure != null) onFailure.run();
            return;
        }

        getCartRef().document(bookId)
                .delete()
                .addOnSuccessListener(unused -> {
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.run();
                });
    }

    /**
     * 🟡 Cập nhật số lượng sản phẩm trong giỏ
     */
    public void updateQuantity(String bookId, int newQuantity, Runnable onSuccess, Runnable onFailure) {
        if (userId == null) {
            if (onFailure != null) onFailure.run();
            return;
        }

        DocumentReference docRef = getCartRef().document(bookId);
        docRef.update("quantity", newQuantity)
                .addOnSuccessListener(unused -> {
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.run();
                });
    }

    /**
     * 🧹 Xóa toàn bộ giỏ hàng (sử dụng batch)
     */
    public void clearCart(Runnable onSuccess, Runnable onFailure) {
        if (userId == null) {
            if (onFailure != null) onFailure.run();
            return;
        }

        getCartRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                WriteBatch batch = db.batch();
                for (DocumentSnapshot doc : task.getResult()) {
                    batch.delete(doc.getReference());
                }

                batch.commit()
                        .addOnSuccessListener(unused -> {
                            if (onSuccess != null) onSuccess.run();
                        })
                        .addOnFailureListener(e -> {
                            if (onFailure != null) onFailure.run();
                        });
            } else {
                if (onFailure != null) onFailure.run();
            }
        });
    }
}

package com.example.book_store_mobileapp.network;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.book_store_mobileapp.data.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
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
        Log.d(TAG, "UserID hiện tại: " + userId);
    }

    public CollectionReference getCartRef() {
        return db.collection("users")
                .document(userId)
                .collection("cart");
    }

    public void addToCart(Book book, int quantity, Runnable onSuccess, Runnable onFailure) {
        if (userId == null) {
            Log.e(TAG, " Không thể thêm vào giỏ hàng - userId null (chưa đăng nhập).");
            if (onFailure != null) onFailure.run();
            return;
        }

        Log.d(TAG, " Đang thêm sách vào giỏ hàng: " + book.getName());

        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("bookId", book.getBookId());
        cartItem.put("productName", book.getName());
        cartItem.put("author", book.getAuthor());
        cartItem.put("briefDescription", book.getBriefDescription());
        cartItem.put("fullDescription", book.getFullDescription());
        cartItem.put("categoryId", book.getCategoryId());
        cartItem.put("imageURL", book.getImageUrl());
        cartItem.put("isbn", book.getIsbn());
        cartItem.put("price", book.getPrice());
        cartItem.put("stock", book.getStock());
        cartItem.put("technicalSpecifications", book.getTechnicalSpecifications());
        cartItem.put("quantity", quantity);

        getCartRef()
                .whereEqualTo("bookId", book.getBookId())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String docId = query.getDocuments().get(0).getId();
                        Log.d(TAG, " Sách đã có trong giỏ hàng, cập nhật số lượng...");
                        getCartRef().document(docId)
                                .update("quantity", FieldValue.increment(quantity))
                                .addOnSuccessListener(unused -> {
                                    Log.d(TAG, "Cập nhật số lượng thành công.");
                                    if (onSuccess != null) onSuccess.run();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, " Lỗi khi cập nhật số lượng: ", e);
                                    if (onFailure != null) onFailure.run();
                                });
                    } else {
                        // Nếu chưa có, thêm mới
                        Log.d(TAG, "Thêm sách mới vào giỏ hàng...");
                        getCartRef()
                                .add(cartItem)
                                .addOnSuccessListener(unused -> {
                                    Log.d(TAG, " Thêm mới thành công vào Firestore!");
                                    if (onSuccess != null) onSuccess.run();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, " Lỗi khi thêm mới sản phẩm vào giỏ hàng: ", e);
                                    if (onFailure != null) onFailure.run();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Lỗi khi kiểm tra tồn tại trong giỏ hàng: ", e);
                    if (onFailure != null) onFailure.run();
                });
    }



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

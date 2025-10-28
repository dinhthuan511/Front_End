package com.example.book_store_mobileapp.network;

import static android.content.ContentValues.TAG;
import android.util.Log;
import com.example.book_store_mobileapp.data.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
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
        if (userId == null) return null;
        return db.collection("carts")
                .document(userId)
                .collection("items");
    }

    /** 🛒 Thêm sách vào giỏ hàng */
    public void addToCart(Book book, int quantity, Runnable onSuccess, Runnable onFailure) {
        if (userId == null) {
            Log.e(TAG, "❌ Không thể thêm vào giỏ hàng - userId null (chưa đăng nhập).");
            if (onFailure != null) onFailure.run();
            return;
        }

        Log.d(TAG, "🟢 Đang thêm sách: " + book.getName());

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

//         Kiểm tra xem sách đã tồn tại chưa
        getCartRef().whereEqualTo("bookId", book.getBookId())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        // Nếu có rồi => tăng số lượng
                        DocumentSnapshot existing = query.getDocuments().get(0);
                        existing.getReference().update("quantity", FieldValue.increment(quantity))
                                .addOnSuccessListener(unused -> {
                                    Log.d(TAG, "✅ Cập nhật số lượng thành công.");
                                    if (onSuccess != null) onSuccess.run();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ Lỗi cập nhật số lượng: ", e);
                                    if (onFailure != null) onFailure.run();
                                });
                    } else {
                        // Nếu chưa có => thêm mới
                        getCartRef().add(cartItem)
                                .addOnSuccessListener(unused -> {
                                    Log.d(TAG, "✅ Thêm sản phẩm mới thành công!");
                                    if (onSuccess != null) onSuccess.run();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ Lỗi thêm mới: ", e);
                                    if (onFailure != null) onFailure.run();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Lỗi kiểm tra sản phẩm: ", e);
                    if (onFailure != null) onFailure.run();
                });

    }

    /** 🔢 Cập nhật số lượng (theo documentId) */
    public void updateQuantity(String cartItemId, int newQuantity, Runnable onSuccess, Runnable onFailure) {
        if (userId == null) {
            if (onFailure != null) onFailure.run();
            return;
        }

        getCartRef().document(cartItemId)
                .update("quantity", newQuantity)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "✅ Cập nhật số lượng: " + newQuantity);
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Lỗi khi cập nhật số lượng: ", e);
                    if (onFailure != null) onFailure.run();
                });
    }

    /** ❌ Xóa sản phẩm khỏi giỏ (theo documentId) */
    public void removeCartItemById(String cartItemId, Runnable onSuccess, Runnable onFailure) {
        if (userId == null) {
            if (onFailure != null) onFailure.run();
            return;
        }

        getCartRef().document(cartItemId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "✅ Đã xóa sản phẩm khỏi giỏ (ID: " + cartItemId + ")");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Lỗi khi xóa sản phẩm: ", e);
                    if (onFailure != null) onFailure.run();
                });
    }
    /** 🧹 Xóa toàn bộ giỏ hàng */
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
                            Log.d(TAG, "✅ Đã xóa toàn bộ giỏ hàng.");
                            if (onSuccess != null) onSuccess.run();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Lỗi khi xóa toàn bộ giỏ hàng: ", e);
                            if (onFailure != null) onFailure.run();
                        });
            } else {
                if (onFailure != null) onFailure.run();
            }
        });
    }

    /** 🔢 Get total cart item count */
    public interface CartCountCallback {
        void onCartCount(int count);
    }

    public void getCartItemCount(CartCountCallback callback) {
        if (userId == null) {
            callback.onCartCount(0);
            return;
        }

        getCartRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int totalCount = 0;
                for (DocumentSnapshot doc : task.getResult()) {
                    Long quantity = doc.getLong("quantity");
                    if (quantity != null) {
                        totalCount += quantity.intValue();
                    }
                }
                callback.onCartCount(totalCount);
            } else {
                callback.onCartCount(0);
            }
        });
    }
}

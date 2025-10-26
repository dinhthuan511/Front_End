package com.example.book_store_mobileapp.network;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;

/**
 * FirebaseAdminService
 * --------------------
 * Gom toàn bộ logic CRUD cho collection "products":
 *  - addProduct(Map) -> thêm sản phẩm
 *  - updateProduct(String, Map) -> cập nhật
 *  - deleteProduct(String) -> xoá
 *  - listenProducts(int limit, ...) -> lắng nghe realtime danh sách sản phẩm
 *
 * Hoàn toàn tách khỏi UI. Activity chỉ gọi service này.
 * Dùng chung ApiResponse của m (isSuccess(), getMessage(), getData()).
 */
public class FirebaseAdminService {

    // Có thể cho collectionName thành tham số nếu muốn tái sử dụng
    private static final String PRODUCTS = "products";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference col() { return db.collection(PRODUCTS); }

    // Callback kiểu auth service
    public interface Callback<T> { void onResult(ApiResponse<T> result); }

    // Listener trả danh sách DocumentSnapshot mỗi lần dữ liệu thay đổi
    public interface ProductsListener {
        void onChanged(List<DocumentSnapshot> docs);
        void onError(String message);
    }

    /** Thêm sản phẩm, trả về documentId */
    public void addProduct(@NonNull Map<String, Object> doc, @NonNull Callback<String> cb) {
        col().add(doc)
                .addOnSuccessListener(ref -> cb.onResult(ApiResponse.success(ref.getId())))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }

    /** Cập nhật sản phẩm theo docId */
    public void updateProduct(@NonNull String docId, @NonNull Map<String, Object> updates,
                              @NonNull Callback<Void> cb) {
        col().document(docId).update(updates)
                .addOnSuccessListener(v -> cb.onResult(ApiResponse.success(null)))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }

    /** Xoá sản phẩm theo docId */
    public void deleteProduct(@NonNull String docId, @NonNull Callback<Void> cb) {
        col().document(docId).delete()
                .addOnSuccessListener(v -> cb.onResult(ApiResponse.success(null)))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }

    /**
     * Lắng nghe realtime danh sách sản phẩm.
     * Trả về ListenerRegistration để Activity giữ và gọi remove() ở onDestroy().
     */
    public ListenerRegistration listenProducts(int limit, @NonNull ProductsListener listener) {
        return col().limit(limit <= 0 ? 100 : limit)
                .addSnapshotListener((snaps, e) -> {
                    if (e != null) {
                        listener.onError(safeMsg(e));
                        return;
                    }
                    if (snaps == null) {
                        listener.onChanged(java.util.Collections.emptyList());
                        return;
                    }
                    listener.onChanged(snaps.getDocuments());
                });
    }

    /** (tuỳ chọn) Lấy 1 sản phẩm */
    public void getProductById(@NonNull String docId, @NonNull Callback<DocumentSnapshot> cb) {
        col().document(docId).get()
                .addOnSuccessListener(doc -> cb.onResult(ApiResponse.success(doc)))
                .addOnFailureListener(e -> cb.onResult(ApiResponse.error(safeMsg(e))));
    }

    private String safeMsg(Exception e) {
        return (e != null && e.getMessage() != null) ? e.getMessage() : "Unknown error";
    }
}

package com.example.book_store_mobileapp.network;

import androidx.annotation.NonNull;

import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.data.BookCategory;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Dịch vụ làm việc với Firestore để lấy danh sách sách.
 */
public class FirebaseBookService {

    private static FirebaseBookService instance;
    private final FirebaseFirestore db;

    private FirebaseBookService() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirebaseBookService getInstance() {
        if (instance == null) instance = new FirebaseBookService();
        return instance;
    }

    private CollectionReference getBookRef() {
        return db.collection("products");
    }

    private CollectionReference getCategoryRef() { return db.collection("categories");}

    // ===== Helpers ============================================================

    // CHANGED: Resolve ảnh từ nhiều kiểu field khác nhau
    private String resolveImage(DocumentSnapshot doc) {                        // NEW
        // 1) imageUrl (đúng chuẩn camelCase)
        String url = doc.getString("imageUrl");
        if (url == null || url.isEmpty()) {
            // 2) imageURL (viết hoa L)
            url = doc.getString("imageURL");                                   // NEW
        }
        if (url == null || url.isEmpty()) {
            // 3) ImageUrl (I hoa đầu)
            url = doc.getString("ImageUrl");                                   // NEW
        }

        // Nếu "url" thực ra chứa base64 thô (không có prefix data:)
        if (url != null && !url.isEmpty()
                && !url.startsWith("http") && !url.startsWith("data:")
                && looksLikeBase64(url)) {                                     // NEW
            return "data:image/jpeg;base64," + url;
        }

        if (url != null && !url.isEmpty()) return url;

        // 4) Fallback: imageBase64
        String b64 = doc.getString("imageBase64");                             // NEW
        if (b64 != null && !b64.isEmpty()) {
            return "data:image/jpeg;base64," + b64;
        }

        return null; // không có ảnh
    }

    private boolean looksLikeBase64(String s) {                                 // NEW
        if (s == null) return false;
        if (s.length() < 50) return false; // base64 ảnh thường khá dài
        return s.matches("^[A-Za-z0-9+/=\\r\\n]+$");
    }

    // ===== API ================================================================

    /**
     * 🟢 Lấy tất cả sách từ Firestore
     */
    public void getAllBooks(@NonNull FirestoreCallback<List<Book>> listener) {
        getBookRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Book> books = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {

                    String image = resolveImage(doc);                           // CHANGED

                    Book book = new Book(
                            doc.getId(),
                            doc.getString("productName"),
                            doc.getString("author"),
                            doc.getString("briefDescription"),
                            doc.getString("fullDescription"),
                            doc.getLong("categoryId"),
                            image,                                              // CHANGED
                            doc.getString("isbn"),
                            doc.getDouble("price"),
                            doc.getLong("stock"),
                            doc.getString("technicalSpecifications")
                    );
                    books.add(book);
                }
                listener.onSuccess(books);
            } else {
                listener.onError("Không thể tải danh sách sách");
            }
        });
    }

    /**
     * 🟡 Lấy tất cả thể loại từ Firestore
     */
    public void getAllCategories(@NonNull FirestoreCallback<List<BookCategory>> listener) {
        getCategoryRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<BookCategory> categories = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    BookCategory category = new BookCategory(
                            doc.getId(),
                            doc.getString("categoryName"));
                    categories.add(category);
                }
                listener.onSuccess(categories);
            } else {
                listener.onError("Không thể tải danh sách thể loại");
            }
        });
    }

    /**
     * 🟡 Lấy chi tiết 1 sách theo ID
     */
    public void getBookById(String bookId, @NonNull FirestoreCallback<Book> listener) {
        getBookRef().document(bookId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();

                String image = resolveImage(doc);                               // CHANGED

                Book book = new Book(
                        doc.getId(),
                        doc.getString("productName"),
                        doc.getString("author"),
                        doc.getString("briefDescription"),
                        doc.getString("fullDescription"),
                        doc.getLong("categoryId"),
                        image,                                                  // CHANGED
                        doc.getString("isbn"),
                        doc.getDouble("price"),
                        doc.getLong("stock"),
                        doc.getString("technicalSpecifications")
                );
                listener.onSuccess(book);
            } else {
                listener.onError("Không tìm thấy sách");
            }
        });
    }

    // 🔵 Callback chung cho Firestore
    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}

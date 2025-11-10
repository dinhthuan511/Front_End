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

    private CollectionReference getCategoryRef() {
        return db.collection("categories");
    }

    // ===== Helpers ============================================================

    /**
     * ✅ Trích xuất danh sách ảnh (có thể là imageBase64 array hoặc imageUrl string)
     */
    private List<String> resolveImages(DocumentSnapshot doc) {
        List<String> images = new ArrayList<>();

        // 1️⃣ Nếu có imageBase64 là mảng
        Object rawImages = doc.get("imageBase64");
        if (rawImages instanceof List<?>) {
            for (Object o : (List<?>) rawImages) {
                if (o instanceof String && !((String) o).isEmpty()) {
                    String img = (String) o;
                    if (!img.startsWith("http") && !img.startsWith("data:")) {
                        img = "data:image/jpeg;base64," + img;
                    }
                    images.add(img);
                }
            }
        }

        // 2️⃣ fallback nếu có imageUrl / imageURL / ImageUrl
        String singleUrl = doc.getString("imageUrl");
        if (singleUrl == null || singleUrl.isEmpty()) singleUrl = doc.getString("imageURL");
        if (singleUrl == null || singleUrl.isEmpty()) singleUrl = doc.getString("ImageUrl");
        if (singleUrl != null && !singleUrl.isEmpty()) {
            if (!singleUrl.startsWith("http") && !singleUrl.startsWith("data:")) {
                singleUrl = "data:image/jpeg;base64," + singleUrl;
            }
            images.add(singleUrl);
        }

        // ❌ Xóa phần này, vì `imageBase64` không còn là string:
        // String singleBase64 = doc.getString("imageBase64");
        // if (singleBase64 != null && !singleBase64.isEmpty()) {
        //     if (!singleBase64.startsWith("http") && !singleBase64.startsWith("data:")) {
        //         singleBase64 = "data:image/jpeg;base64," + singleBase64;
        //     }
        //     images.add(singleBase64);
        // }

        return images;
    }

    // ===== API ================================================================

    /** 🟢 Lấy tất cả sách từ Firestore */
    public void getAllBooks(@NonNull FirestoreCallback<List<Book>> listener) {
        getBookRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Book> books = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    List<String> images = resolveImages(doc);

                    Book book = new Book(
                            doc.getId(),
                            doc.getString("productName"),
                            doc.getString("author"),
                            doc.getString("briefDescription"),
                            doc.getString("fullDescription"),
                            doc.getLong("categoryId"),
                            images, // ✅ TRUYỀN MẢNG ẢNH
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

    /** 🟡 Lấy tất cả thể loại từ Firestore */
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

    /** 🟡 Lấy chi tiết 1 sách theo ID */
    public void getBookById(String bookId, @NonNull FirestoreCallback<Book> listener) {
        getBookRef().document(bookId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();

                List<String> images = resolveImages(doc); // ✅ cập nhật

                Book book = new Book(
                        doc.getId(),
                        doc.getString("productName"),
                        doc.getString("author"),
                        doc.getString("briefDescription"),
                        doc.getString("fullDescription"),
                        doc.getLong("categoryId"),
                        images, // ✅ truyền mảng
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

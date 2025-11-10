package com.example.book_store_mobileapp.network;

import android.util.Log;

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
        return db.collection("products"); // ✅ đúng tên collection của bạn
    }

    private CollectionReference getCategoryRef() {
        return db.collection("categories");
    }

    // ============================================================

    /** ✅ Trích xuất danh sách ảnh (mảng URL hoặc base64) */
    private List<String> resolveImages(DocumentSnapshot doc) {
        List<String> images = new ArrayList<>();

        Object rawImages = doc.get("imageBase64");
        if (rawImages instanceof List<?>) {
            for (Object o : (List<?>) rawImages) {
                if (o instanceof String && !((String) o).isEmpty()) {
                    String img = (String) o;
                    // Nếu là URL thì giữ nguyên, nếu là base64 thì thêm prefix
                    if (!img.startsWith("http") && !img.startsWith("data:")) {
                        img = "data:image/jpeg;base64," + img;
                    }
                    images.add(img);
                }
            }
        }

        return images;
    }

    // ============================================================

    /** 🟢 Lấy tất cả sách từ Firestore */
    public void getAllBooks(@NonNull FirestoreCallback<List<Book>> listener) {
        getBookRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Book> books = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    Log.d("BOOK_FIRESTORE", "Doc: " + doc.getData());

                    List<String> images = resolveImages(doc);

                    // ✅ Sử dụng đúng field name trong Firestore
                    Double price = doc.getDouble("price");
                    Long stock = doc.getLong("stock");
                    Long categoryId = doc.getLong("categoryId");

                    Book book = new Book(
                            doc.getId(),
                            doc.getString("productName"),
                            doc.getString("author"),
                            doc.getString("briefDescription"),
                            doc.getString("fullDescription"),
                            categoryId != null ? categoryId : 0,
                            images,
                            doc.getString("isbn"),
                            price != null ? price : 0.0,
                            stock != null ? stock : 0,
                            doc.getString("technicalSpecifications")
                    );
                    books.add(book);
                }
                listener.onSuccess(books);
            } else {
                Log.e("BOOK_FIRESTORE", "Error loading books", task.getException());
                listener.onError("Không thể tải danh sách sách");
            }
        });
    }

    /** 🟡 Lấy tất cả thể loại */
    public void getAllCategories(@NonNull FirestoreCallback<List<BookCategory>> listener) {
        getCategoryRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<BookCategory> categories = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    categories.add(new BookCategory(
                            doc.getId(),
                            doc.getString("categoryName")
                    ));
                }
                listener.onSuccess(categories);
            } else {
                listener.onError("Không thể tải danh sách thể loại");
            }
        });
    }

    /** 🟢 Lấy chi tiết 1 sách */
    public void getBookById(String bookId, @NonNull FirestoreCallback<Book> listener) {
        getBookRef().document(bookId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();
                List<String> images = resolveImages(doc);

                Double price = doc.getDouble("price");
                Long stock = doc.getLong("stock");
                Long categoryId = doc.getLong("categoryId");

                Book book = new Book(
                        doc.getId(),
                        doc.getString("productName"),
                        doc.getString("author"),
                        doc.getString("briefDescription"),
                        doc.getString("fullDescription"),
                        categoryId != null ? categoryId : 0,
                        images,
                        doc.getString("isbn"),
                        price != null ? price : 0.0,
                        stock != null ? stock : 0,
                        doc.getString("technicalSpecifications")
                );
                listener.onSuccess(book);
            } else {
                listener.onError("Không tìm thấy sách");
            }
        });
    }

    // ============================================================

    /** 🔵 Callback chung cho Firestore */
    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}

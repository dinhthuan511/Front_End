package com.example.book_store_mobileapp.network;

import androidx.annotation.NonNull;

import com.example.book_store_mobileapp.data.Book;
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
        return db.collection("books");
    }

    /**
     * 🟢 Lấy tất cả sách từ Firestore
     */
    public void getAllBooks(@NonNull FirestoreCallback<List<Book>> listener) {
        getBookRef().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Book> books = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    Book book = new Book(
                            doc.getId(), // bookId
                            doc.getString("productName") != null ? doc.getString("productName") : doc.getString("name"),
                            doc.getString("author"),
                            doc.getString("description"),
                            doc.getString("imageURL") != null ? doc.getString("imageURL") : doc.getString("imageUrl"),
                            doc.getDouble("price")
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
     * 🟡 Lấy chi tiết 1 sách theo ID
     */
    public void getBookById(String bookId, @NonNull FirestoreCallback<Book> listener) {
        getBookRef().document(bookId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();
                Book book = new Book(
                        doc.getId(),
                        doc.getString("productName") != null ? doc.getString("productName") : doc.getString("name"),
                        doc.getString("author"),
                        doc.getString("description"),
                        doc.getString("imageURL") != null ? doc.getString("imageURL") : doc.getString("imageUrl"),
                        doc.getDouble("price")
                );
                listener.onSuccess(book);
            } else {
                listener.onError("Không tìm thấy sách");
            }
        });
    }

    /**
     * 🔵 Callback chung cho Firestore
     */
    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}

package com.example.book_store_mobileapp.data;

public class OrderItem {
    private Book book;
    private int quantity;

    public OrderItem() {
        // Bắt buộc cho Firestore
    }

    public OrderItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // ✅ Hàm tiện ích
    public String getBookTitle() {
        return (book != null && book.getName() != null) ? book.getName() : "Sách không rõ";
    }

    public double getPrice() {
        return (book != null) ? book.getPrice() : 0.0;
    }

    public String getImageUrl() {
        return (book != null) ? book.getImageUrl() : null;
    }
}

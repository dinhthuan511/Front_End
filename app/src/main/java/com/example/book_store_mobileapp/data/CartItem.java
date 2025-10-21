package com.example.book_store_mobileapp.data;

public class CartItem {
    private  String cartId;
    private Book book;
    private int quantity;

    public CartItem() {}
    public CartItem(String cartId, Book book, int quantity) {
        this.cartId = cartId;
        this.book = book;
        this.quantity = quantity;
    }

    public Book getBook() {
        return book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return book.getPrice() * quantity;
    }

}


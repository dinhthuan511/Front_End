package com.example.book_store_mobileapp.data;

public class Book {
    private String bookId;
    private String name;
    private String author;
    private String description;
    private String imageUrl;
    private Double price;

    // Constructor
    public Book(String name, String bookId, String author, String description, String imageUrl, Double price) {
        this.name = name;
        this.bookId = bookId;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    // Getters and setters

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}

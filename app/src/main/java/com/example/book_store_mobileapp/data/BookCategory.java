package com.example.book_store_mobileapp.data;

public class BookCategory {
    private String id;
    private String categoryName;

    public BookCategory() {
    }

    public BookCategory(String id, String categoryName) {
        this.id = id;
        this.categoryName = categoryName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}

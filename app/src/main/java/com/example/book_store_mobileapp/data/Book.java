package com.example.book_store_mobileapp.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    private String bookId;
    private String name; // productName
    private String author;
    private String briefDescription;
    private String fullDescription;
    private Long categoryId;
    private String imageUrl; // imageURL
    private String isbn;
    private Double price;
    private Long stock;
    private String technicalSpecifications;
    private int quantity; // For cart logic

    public Book() {
        this.quantity = 1; // Default quantity
    }

    // Constructor with all fields
    public Book(String bookId, String name, String author, String briefDescription, String fullDescription,
                Long categoryId, String imageUrl, String isbn, Double price, Long stock, String technicalSpecifications) {
        this.bookId = bookId;
        this.name = name;
        this.author = author;
        this.briefDescription = briefDescription;
        this.fullDescription = fullDescription;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.isbn = isbn;
        this.price = price;
        this.stock = stock;
        this.technicalSpecifications = technicalSpecifications;
        this.quantity = 1; // Default quantity
    }

    // Parcelable constructor
    protected Book(Parcel in) {
        bookId = in.readString();
        name = in.readString();
        author = in.readString();
        briefDescription = in.readString();
        fullDescription = in.readString();
        if (in.readByte() == 0) {
            categoryId = null;
        } else {
            categoryId = in.readLong();
        }
        imageUrl = in.readString();
        isbn = in.readString();
        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readDouble();
        }
        if (in.readByte() == 0) {
            stock = null;
        } else {
            stock = in.readLong();
        }
        technicalSpecifications = in.readString();
        quantity = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookId);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(briefDescription);
        dest.writeString(fullDescription);
        if (categoryId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(categoryId);
        }
        dest.writeString(imageUrl);
        dest.writeString(isbn);
        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }
        if (stock == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(stock);
        }
        dest.writeString(technicalSpecifications);
        dest.writeInt(quantity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    // Getters and Setters
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getBriefDescription() { return briefDescription; }
    public void setBriefDescription(String briefDescription) { this.briefDescription = briefDescription; }
    public String getFullDescription() { return fullDescription; }
    public void setFullDescription(String fullDescription) { this.fullDescription = fullDescription; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }
    public String getTechnicalSpecifications() { return technicalSpecifications; }
    public void setTechnicalSpecifications(String technicalSpecifications) { this.technicalSpecifications = technicalSpecifications; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

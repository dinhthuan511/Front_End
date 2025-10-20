package com.example.book_store_mobileapp.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    private String bookId;
    private String name;
    private String author;
    private String description;
    private String imageUrl;
    private Double price;
    private int quantity; // 🟢 thêm để dùng trong giỏ hàng

    public Book() {
        this.quantity = 1; // mặc định 1
    }

    // Constructor
    public Book(String bookId, String name, String author, String description, String imageUrl, Double price) {
        this.bookId = bookId;
        this.name = name;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = 1; // mặc định 1
    }

    // Parcelable constructor
    protected Book(Parcel in) {
        bookId = in.readString();
        name = in.readString();
        author = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readDouble();
        }
        quantity = in.readInt();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookId);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(description);
        dest.writeString(imageUrl);
        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }
        dest.writeInt(quantity);
    }

    // Getters & Setters
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    // 🟢 quantity (dùng riêng cho giỏ hàng)
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

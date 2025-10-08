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

    // Constructor
    public Book(String name, String bookId, String author, String description, String imageUrl, Double price) {
        this.name = name;
        this.bookId = bookId;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    // Parcelable constructor to read data from Parcel
    protected Book(Parcel in){
        bookId = in.readString();
        name = in.readString();
        author = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        if(in.readByte() == 0){
            price = null;
        } else {
            price = in.readDouble();
        }
    }

    // Creator for Parcelable
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

    // Describe content
    @Override
    public int describeContents() {
        return 0;
    }

    // Write data to Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(bookId);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(description);
        dest.writeString(imageUrl);
        if(price == null){
            dest.writeByte((byte)0);
        } else {
            dest.writeByte((byte)1);
            dest.writeDouble(price);
        }
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

package com.example.book_store_mobileapp.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Book implements Parcelable {
    private String bookId;
    private String name;
    private String author;
    private String briefDescription;
    private String fullDescription;
    private Long categoryId;
    private List<String> imageBase64; // ✅ Đổi từ String imageUrl sang List<String> imageBase64
    private String isbn;
    private Double price;
    private Long stock;
    private String technicalSpecifications;

    public Book() {}

    public Book(String bookId, String name, String author, String briefDescription, String fullDescription,
                Long categoryId, List<String> imageBase64, String isbn, Double price, Long stock, String technicalSpecifications) {
        this.bookId = bookId;
        this.name = name;
        this.author = author;
        this.briefDescription = briefDescription;
        this.fullDescription = fullDescription;
        this.categoryId = categoryId;
        this.imageBase64 = imageBase64;
        this.isbn = isbn;
        this.price = price;
        this.stock = stock;
        this.technicalSpecifications = technicalSpecifications;
    }

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
        imageBase64 = in.createStringArrayList(); // ✅ đọc mảng ảnh
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
        dest.writeStringList(imageBase64); // ✅ ghi mảng ảnh
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

    // ✅ Getters và Setters
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

    public List<String> getImageBase64() { return imageBase64; } // ✅
    public void setImageBase64(List<String> imageBase64) { this.imageBase64 = imageBase64; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }
    public String getTechnicalSpecifications() { return technicalSpecifications; }
    public void setTechnicalSpecifications(String technicalSpecifications) { this.technicalSpecifications = technicalSpecifications; }
}

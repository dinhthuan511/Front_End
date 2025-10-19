package com.example.book_store_mobileapp.data;

import android.os.Parcel;
import android.os.Parcelable;

// Giữ Parcelable để có thể truyền đối tượng Book qua Intent
public class Book implements Parcelable {

    // ✅ CÁC TRƯỜNG PHẢI KHỚP VỚI TÊN TRONG FIRESTORE DOCUMENT
    private String id; // Dùng để lưu ID của Document
    private String productName;
    private String author;
    private String briefDescription;
    private String fullDescription;
    private String imageURL;
    private String ISBN;
    private long categoryId; // Firestore thường trả về Long cho số nguyên
    private double price;
    private int stock;
    private String technicalSpecifications;

    // ✅ BẮT BUỘC: PHẢI CÓ CONSTRUCTOR RỖNG CHO FIRESTORE
    public Book() {
    }

    // Constructor để đọc dữ liệu từ Parcel (dùng khi nhận Intent)
    protected Book(Parcel in) {
        id = in.readString();
        productName = in.readString();
        author = in.readString();
        briefDescription = in.readString();
        fullDescription = in.readString();
        imageURL = in.readString();
        ISBN = in.readString();
        categoryId = in.readLong();
        price = in.readDouble();
        stock = in.readInt();
        technicalSpecifications = in.readString();
    }

    // ✅ GETTERS VÀ SETTERS CHO TẤT CẢ CÁC TRƯỜNG
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getBriefDescription() { return briefDescription; }
    public void setBriefDescription(String briefDescription) { this.briefDescription = briefDescription; }

    public String getFullDescription() { return fullDescription; }
    public void setFullDescription(String fullDescription) { this.fullDescription = fullDescription; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public String getISBN() { return ISBN; }
    public void setISBN(String ISBN) { this.ISBN = ISBN; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getTechnicalSpecifications() { return technicalSpecifications; }
    public void setTechnicalSpecifications(String technicalSpecifications) { this.technicalSpecifications = technicalSpecifications; }


    // --- CÁC PHƯƠNG THỨC CỦA PARCELABLE ---

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(productName);
        dest.writeString(author);
        dest.writeString(briefDescription);
        dest.writeString(fullDescription);
        dest.writeString(imageURL);
        dest.writeString(ISBN);
        dest.writeLong(categoryId);
        dest.writeDouble(price);
        dest.writeInt(stock);
        dest.writeString(technicalSpecifications);
    }
}

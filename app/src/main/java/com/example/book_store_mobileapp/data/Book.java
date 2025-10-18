// D:/Project/Front_End/app/src/main/java/com/example/book_store_mobileapp/data/Book.java

package com.example.book_store_mobileapp.data;

import android.os.Parcel;
import android.os.Parcelable;

// Triển khai Parcelable để có thể gửi đối tượng này qua Intent (ví dụ: sang BookDetailActivity)
public class Book implements Parcelable {

    // 1. ✅ TÊN THUỘC TÍNH ĐÃ KHỚP VỚI JSON TRÊN FIREBASE
    private String id; // Dùng để lưu key của sản phẩm
    private String productName;
    private String briefDescription;
    private String fullDescription;
    private String technicalSpecifications;
    private double price;
    private String imageURL;
    private String author;
    private String publisher;
    private String isbn;
    private int stock;
    private int categoryID;
    private int quantity; // Dùng cho giỏ hàng

    // 2. ✅ BẮT BUỘC: CONSTRUCTOR RỖNG CHO FIREBASE
    public Book() {
    }

    // Constructor để đọc từ Parcelable (giữ nguyên)
    protected Book(Parcel in) {
        id = in.readString();
        productName = in.readString();
        briefDescription = in.readString();
        fullDescription = in.readString();
        technicalSpecifications = in.readString();
        price = in.readDouble();
        imageURL = in.readString();
        author = in.readString();
        publisher = in.readString();
        isbn = in.readString();
        stock = in.readInt();
        categoryID = in.readInt();
        quantity = in.readInt();
    }

    // 3. ✅ GETTERS và SETTERS CHO TẤT CẢ CÁC THUỘC TÍNH
    // Firebase cần chúng để đọc/ghi dữ liệu vào đối tượng
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBriefDescription() { return briefDescription; }
    public void setBriefDescription(String briefDescription) { this.briefDescription = briefDescription; }

    public String getFullDescription() { return fullDescription; }
    public void setFullDescription(String fullDescription) { this.fullDescription = fullDescription; }

    public String getTechnicalSpecifications() { return technicalSpecifications; }
    public void setTechnicalSpecifications(String technicalSpecifications) { this.technicalSpecifications = technicalSpecifications; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getCategoryID() { return categoryID; }
    public void setCategoryID(int categoryID) { this.categoryID = categoryID; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // --- CÁC PHƯƠNG THỨC CỦA PARCELABLE (giữ nguyên) ---

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
        dest.writeString(briefDescription);
        dest.writeString(fullDescription);
        dest.writeString(technicalSpecifications);
        dest.writeDouble(price);
        dest.writeString(imageURL);
        dest.writeString(author);
        dest.writeString(publisher);
        dest.writeString(isbn);
        dest.writeInt(stock);
        dest.writeInt(categoryID);
        dest.writeInt(quantity);
    }
}

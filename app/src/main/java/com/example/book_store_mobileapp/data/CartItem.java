package com.example.book_store_mobileapp.data;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String cartId;
    private Book book;
    private int quantity;

    public CartItem() {}

    public CartItem(String cartId, Book book, int quantity) {
        this.cartId = cartId;
        this.book = book;
        this.quantity = quantity;
    }

    protected CartItem(Parcel in) {
        cartId = in.readString();
        book = in.readParcelable(Book.class.getClassLoader());
        quantity = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cartId);
        dest.writeParcelable(book, flags);
        dest.writeInt(quantity);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

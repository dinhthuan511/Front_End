package com.example.book_store_mobileapp.data;

import com.google.firebase.Timestamp;
import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String name;
    private String phone;
    private String address;
    private String paymentMethod;
    private String paymentStatus;
    private String status;
    private String bankAccount;
    private Timestamp createdAt; // ✅ Dùng đúng Firebase Timestamp
    private double total;
    private List<OrderItem> items;

    public Order() {}

    public Order(String orderId, String userId, String name, String phone, String address,
                 String paymentMethod, String paymentStatus, String status,
                 String bankAccount, Timestamp createdAt, double  total, List<OrderItem> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.status = status;
        this.bankAccount = bankAccount;
        this.createdAt = createdAt;
        this.total = total;
        this.items = items;
    }

    // Getter - Setter
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public double  getTotal() { return total; }
    public void setTotal(double  total) { this.total = total; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

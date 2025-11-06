package com.example.book_store_mobileapp.data;

import com.google.firebase.Timestamp;
import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String name;
    private String phone;
    private String address;
    private String email;            // ✅ Thêm email (dùng hiển thị, readonly)
    private double total;

    private String paymentMethod;    // COD / ZaloPay
    private String paymentStatus;    // Thành công / Thất bại / Chưa thanh toán
    private String status;           // CHỜ XÁC NHẬN / ĐANG GIAO / ĐÃ GIAO / ĐÃ HỦY

    private Timestamp createdAt;
    private String bankAccount;      // Dùng nếu là chuyển khoản manual (bank app)

    private List<OrderItem> items;   // ✅ Dạng chứa Book object

    // 🔹 ZaloPay transaction fields
    private String transactionId;   // Mã giao dịch ZaloPay (transactionId)
    private String appTransId;      // appTransId gửi server
    private String paymentToken;    // zp_trans_token

    // 🔹 Trường hủy đơn cho admin
    private String cancelReason;
    private Timestamp cancelledAt;

    public Order() {
        // Firestore requires empty constructor
    }

    public Order(String orderId, String userId, String name, String phone, String address, String email,
                 double total, String paymentMethod, String paymentStatus, String status,
                 Timestamp createdAt, String bankAccount, List<OrderItem> items,
                 String transactionId, String appTransId, String paymentToken,
                 String cancelReason, Timestamp cancelledAt) {

        this.orderId = orderId;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.status = status;
        this.createdAt = createdAt;
        this.bankAccount = bankAccount;
        this.items = items;
        this.transactionId = transactionId;
        this.appTransId = appTransId;
        this.paymentToken = paymentToken;
        this.cancelReason = cancelReason;
        this.cancelledAt = cancelledAt;
    }

    // ✅ Getter - Setter
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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getAppTransId() { return appTransId; }
    public void setAppTransId(String appTransId) { this.appTransId = appTransId; }

    public String getPaymentToken() { return paymentToken; }
    public void setPaymentToken(String paymentToken) { this.paymentToken = paymentToken; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public Timestamp getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Timestamp cancelledAt) { this.cancelledAt = cancelledAt; }
}

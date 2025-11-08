package com.example.book_store_mobileapp.data;

public class UserChatInfo {
    private String userId;
    private String userEmail;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadCount;

    public UserChatInfo() {
        // Default constructor required for Firebase
    }

    public UserChatInfo(String userId, String userEmail, String lastMessage, long lastMessageTime) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
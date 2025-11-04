package com.example.book_store_mobileapp.data.chat;

import java.util.Date;

public class Message {
    private String senderId;
    private String text;
    private long timestamp;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String senderId, String text) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = new Date().getTime();
    }

    public String getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

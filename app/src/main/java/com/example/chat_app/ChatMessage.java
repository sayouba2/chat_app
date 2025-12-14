package com.example.chat_app;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String senderId;
    private String receiverId;
    private String message;
    private Timestamp timestamp;


    public ChatMessage() { } // Requis pour Firebase

    public ChatMessage(String senderId, String receiverId, String message, Timestamp timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public Timestamp getTimestamp() { return timestamp; }
}
package com.example.chat_app;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String senderId;
    private String receiverId;
    private String message;
    private Timestamp timestamp;
    private boolean isSeen;
    private String type;
    public ChatMessage() { } // Requis pour Firebase
    public ChatMessage(String senderId, String receiverId, String message, Timestamp timestamp, String type, boolean isSeen) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.isSeen = isSeen;
    }
    public String getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public boolean isSeen() { return isSeen; }
    public void setSeen(boolean seen) { isSeen = seen; }
}
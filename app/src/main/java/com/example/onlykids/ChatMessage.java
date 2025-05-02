package com.example.onlykids;

public class ChatMessage {
    private String message;
    private boolean isUser; // Indicates if the message is from the user (true) or assistant (false)

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUserMessage() {
        return isUser;
    }
}
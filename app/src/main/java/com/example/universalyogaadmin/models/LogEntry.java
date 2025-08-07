package com.example.universalyogaadmin.models;

public class LogEntry {
    private final String message;
    private final String timestamp;

    public LogEntry(String message, String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }
    public String getTimestamp() {
        return timestamp;
    }
}
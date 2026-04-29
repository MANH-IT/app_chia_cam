package com.chupchia.models;

import java.io.Serializable;

public class EditHistory implements Serializable {
    private String userId;
    private String userName;
    private long timestamp;
    private String changes;
    
    // Default constructor for Firebase/Gson
    public EditHistory() {}
    
    public EditHistory(String userId, String userName, long timestamp, String changes) {
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timestamp;
        this.changes = changes;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getChanges() {
        return changes;
    }
    
    public void setChanges(String changes) {
        this.changes = changes;
    }
}

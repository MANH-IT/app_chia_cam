package com.chupchia.models;

import java.io.Serializable;

public class Member implements Serializable {
    private String id;
    private String name;
    private String avatarUrl;
    private String phoneNumber;
    private String email;
    private String role; // "admin" or "member"
    private boolean isSelected;
    private int customValue; // For percent or custom split
    private long joinedAt;
    private int balance; // Net balance in group (positive = owed to, negative = owes)
    
    public Member() {
        this.joinedAt = System.currentTimeMillis();
        this.balance = 0;
    }
    
    public Member(String id, String name, String avatarUrl, String role) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.isSelected = false;
        this.customValue = 0;
        this.joinedAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
    
    public int getCustomValue() { return customValue; }
    public void setCustomValue(int customValue) { this.customValue = customValue; }
    
    // Alias for split adapters
    public int getSplitValue() { return customValue; }
    public void setSplitValue(int value) { this.customValue = value; }
    
    public long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }
    
    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }
    
    public boolean isAdmin() {
        return "admin".equals(role);
    }
}

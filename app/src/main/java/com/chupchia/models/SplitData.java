package com.chupchia.models;

import java.io.Serializable;

public class SplitData implements Serializable {
    private String userId;
    private String userName;
    private String avatarUrl;
    private long amount;
    private int percentage;
    private boolean isPaid;
    
    public SplitData() {}
    
    public SplitData(String userId, String userName, String avatarUrl, long amount) {
        this.userId = userId;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.amount = amount;
        this.isPaid = false;
    }
    
    public SplitData(String userId, String userName, String avatarUrl, int percentage) {
        this.userId = userId;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.percentage = percentage;
        this.isPaid = false;
    }
    
    // Getter và Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    
    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }
    
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
}

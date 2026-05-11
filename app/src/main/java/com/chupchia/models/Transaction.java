package com.chupchia.models;

import java.io.Serializable;

public class Transaction implements Serializable {
    private String fromUserId;
    private String toUserId;
    private String fromUserName;
    private String toUserName;
    private long amount;
    private String reason;
    
    public Transaction() {}
    
    public Transaction(String fromUserId, String toUserId, long amount) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
    }
    
    public Transaction(String fromUserId, String toUserId, long amount, String reason) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.reason = reason;
    }
    
    public Transaction(String fromUserId, String fromUserName, String toUserId, 
                       String toUserName, long amount) {
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.toUserId = toUserId;
        this.toUserName = toUserName;
        this.amount = amount;
    }
    
    // Getter và Setter
    public String getFromUserId() {
        return fromUserId;
    }
    
    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }
    
    public String getToUserId() {
        return toUserId;
    }
    
    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }
    
    public String getFromUserName() {
        return fromUserName;
    }
    
    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }
    
    public String getToUserName() {
        return toUserName;
    }
    
    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
        this.amount = amount;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getDisplayText() {
        if (fromUserName != null && toUserName != null) {
            return fromUserName + " chuyển " + formatAmount() + " cho " + toUserName;
        }
        return "Chuyển " + formatAmount();
    }
    
    public String formatAmount() {
        return String.format("%,dđ", amount).replace(",", ".");
    }
}

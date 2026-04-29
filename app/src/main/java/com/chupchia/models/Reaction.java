package com.chupchia.models;

public class Reaction {
    private String billId;
    private String userId;
    private String userName;
    private String type; // 😂, 😭, 👍, ❤️, etc.
    private long timestamp;

    public Reaction() {}

    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

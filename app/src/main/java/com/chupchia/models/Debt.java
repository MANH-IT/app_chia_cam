package com.chupchia.models;

public class Debt {
    private String fromUserId;     // Người nợ
    private String fromUserName;
    private String toUserId;       // Người được nợ
    private String toUserName;
    private long amount;           // Số tiền nợ
    private String billId;         // Nguyên nhân từ bill nào

    public Debt() {}

    public Debt(String fromUserId, String fromUserName, String toUserId, String toUserName, long amount, String billId) {
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.toUserId = toUserId;
        this.toUserName = toUserName;
        this.amount = amount;
        this.billId = billId;
    }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }
    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }
    public String getToUserId() { return toUserId; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }
    public String getToUserName() { return toUserName; }
    public void setToUserName(String toUserName) { this.toUserName = toUserName; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }
}

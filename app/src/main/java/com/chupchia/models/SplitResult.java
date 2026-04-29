package com.chupchia.models;

import java.util.HashMap;
import java.util.Map;

public class SplitResult {
    private String billId;
    private long totalAmount;
    private String splitType;
    private Map<String, Integer> memberAmounts; // memberId -> amount
    private Map<String, Integer> memberPercentages; // memberId -> percentage
    
    public SplitResult() {
        this.memberAmounts = new HashMap<>();
        this.memberPercentages = new HashMap<>();
    }
    
    public String getBillId() {
        return billId;
    }
    
    public void setBillId(String billId) {
        this.billId = billId;
    }
    
    public long getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getSplitType() {
        return splitType;
    }
    
    public void setSplitType(String splitType) {
        this.splitType = splitType;
    }
    
    public Map<String, Integer> getMemberAmounts() {
        return memberAmounts;
    }
    
    public void setMemberAmounts(Map<String, Integer> memberAmounts) {
        this.memberAmounts = memberAmounts;
    }
    
    public void addMemberAmount(String memberId, int amount) {
        this.memberAmounts.put(memberId, amount);
    }
    
    public Map<String, Integer> getMemberPercentages() {
        return memberPercentages;
    }
    
    public void setMemberPercentages(Map<String, Integer> memberPercentages) {
        this.memberPercentages = memberPercentages;
    }
    
    public void addMemberPercentage(String memberId, int percentage) {
        this.memberPercentages.put(memberId, percentage);
    }
    
    public int getMemberAmount(String memberId) {
        return memberAmounts.getOrDefault(memberId, 0);
    }
    
    public int getMemberPercentage(String memberId) {
        return memberPercentages.getOrDefault(memberId, 0);
    }
}

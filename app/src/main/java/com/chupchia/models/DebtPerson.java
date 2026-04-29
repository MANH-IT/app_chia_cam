package com.chupchia.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DebtPerson implements Serializable {
    private String id;
    private String name;
    private String avatarUrl;
    private long amount;
    private List<String> reasons;
    private List<Bill> relatedBills;
    
    public DebtPerson() {
        this.reasons = new ArrayList<>();
        this.relatedBills = new ArrayList<>();
    }
    
    public DebtPerson(String id, String name, String avatarUrl, long amount) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.amount = amount;
        this.reasons = new ArrayList<>();
        this.relatedBills = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
        this.amount = amount;
    }
    
    public List<String> getReasons() {
        return reasons;
    }
    
    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
    
    public void addReason(String reason) {
        if (this.reasons == null) this.reasons = new ArrayList<>();
        this.reasons.add(reason);
    }
    
    public List<Bill> getRelatedBills() {
        return relatedBills;
    }
    
    public void setRelatedBills(List<Bill> relatedBills) {
        this.relatedBills = relatedBills;
    }
    
    public void addRelatedBill(Bill bill) {
        if (this.relatedBills == null) this.relatedBills = new ArrayList<>();
        this.relatedBills.add(bill);
    }
}

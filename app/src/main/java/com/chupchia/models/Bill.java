package com.chupchia.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "bills")
public class Bill implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String imageUrl;
    private String productName;
    private long amount;
    private String actorName;
    private String actorId;
    private String creatorId;
    private String groupAdminId;
    private String groupId;
    private int splitCount;
    private String splitType;
    private long timestamp;
    private String note;
    @Ignore
    private List<EditHistory> editHistory;
    @Ignore
    private Map<String, Integer> reactions;
    
    public Bill() {
        this.editHistory = new ArrayList<>();
        this.reactions = new HashMap<>();
        reactions.put("😂", 0);
        reactions.put("😭", 0);
        reactions.put("👍", 0);
        reactions.put("❤️", 0);
    }
    
    @Ignore
    public Bill(String id, String imageUrl, String productName, long amount, 
                String actorName, String actorId, String creatorId, String groupAdminId,
                String groupId, int splitCount, String splitType, long timestamp, String note) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.productName = productName;
        this.amount = amount;
        this.actorName = actorName;
        this.actorId = actorId;
        this.creatorId = creatorId;
        this.groupAdminId = groupAdminId;
        this.groupId = groupId;
        this.splitCount = splitCount;
        this.splitType = splitType;
        this.timestamp = timestamp;
        this.note = note;
        this.editHistory = new ArrayList<>();
        this.reactions = new HashMap<>();
        reactions.put("😂", 0);
        reactions.put("😭", 0);
        reactions.put("👍", 0);
        reactions.put("❤️", 0);
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    
    public String getGroupAdminId() { return groupAdminId; }
    public void setGroupAdminId(String groupAdminId) { this.groupAdminId = groupAdminId; }
    
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public int getSplitCount() { return splitCount; }
    public void setSplitCount(int splitCount) { this.splitCount = splitCount; }
    
    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public List<EditHistory> getEditHistory() { return editHistory; }
    public void setEditHistory(List<EditHistory> editHistory) { this.editHistory = editHistory; }
    
    public Map<String, Integer> getReactions() { return reactions; }
    public void setReactions(Map<String, Integer> reactions) { this.reactions = reactions; }
    
    public void addEditHistory(EditHistory history) {
        if (this.editHistory == null) {
            this.editHistory = new ArrayList<>();
        }
        this.editHistory.add(0, history);
    }
    
    public int getReactionCount(String reactionType) {
        return reactions.containsKey(reactionType) ? reactions.get(reactionType) : 0;
    }
    
    public void addReaction(String reactionType) {
        int count = getReactionCount(reactionType);
        reactions.put(reactionType, count + 1);
    }
    
    public void removeReaction(String reactionType) {
        int count = getReactionCount(reactionType);
        if (count > 0) {
            reactions.put(reactionType, count - 1);
        }
    }
    
    public long getPerPersonAmount() {
        return amount / (splitCount > 0 ? splitCount : 1);
    }
    
    public String getFormattedAmount() {
        return String.format("%,dđ", amount).replace(",", ".");
    }
    
    public String getFormattedPerPersonAmount() {
        return String.format("%,dđ", getPerPersonAmount()).replace(",", ".");
    }
    
    // Clone method for editing
    public Bill clone() {
        Bill cloned = new Bill();
        cloned.id = this.id;
        cloned.imageUrl = this.imageUrl;
        cloned.productName = this.productName;
        cloned.amount = this.amount;
        cloned.actorName = this.actorName;
        cloned.actorId = this.actorId;
        cloned.creatorId = this.creatorId;
        cloned.groupAdminId = this.groupAdminId;
        cloned.groupId = this.groupId;
        cloned.splitCount = this.splitCount;
        cloned.splitType = this.splitType;
        cloned.timestamp = this.timestamp;
        cloned.note = this.note;
        cloned.editHistory = new ArrayList<>(this.editHistory);
        cloned.reactions = new HashMap<>(this.reactions);
        return cloned;
    }
}

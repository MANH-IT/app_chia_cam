package com.chupchia.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.room.Ignore;

import java.io.Serializable;

@Entity(tableName = "notifications")
public class Notification implements Serializable {
    
    // Notification types
    public static final String TYPE_NEW_BILL = "new_bill";
    public static final String TYPE_REACTION = "reaction";
    public static final String TYPE_MENTION = "mention";
    public static final String TYPE_DEBT_REMINDER = "debt_reminder";
    public static final String TYPE_MEMBER_JOINED = "member_joined";
    public static final String TYPE_GROUP_INVITATION = "group_invitation";
    
    @PrimaryKey
    @NonNull
    private String id;
    private String type;
    private String title;
    private String content;
    private String targetId;      // Bill ID, Group ID, etc.
    private String groupId;
    private String groupName;
    private String actorId;
    private String actorName;
    private long timestamp;
    private boolean isRead;
    private String imageUrl;
    
    public Notification() {}
    
    @Ignore
    public Notification(String id, String type, String title, String content, 
                        String targetId, String groupId, String groupName,
                        String actorId, String actorName, long timestamp, boolean isRead) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.content = content;
        this.targetId = targetId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.actorId = actorId;
        this.actorName = actorName;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    // Helper methods
    public String getIcon() {
        switch (type) {
            case TYPE_NEW_BILL: return "🆕";
            case TYPE_REACTION: return "👍";
            case TYPE_MENTION: return "@";
            case TYPE_DEBT_REMINDER: return "💰";
            case TYPE_MEMBER_JOINED: return "👥";
            case TYPE_GROUP_INVITATION: return "📨";
            default: return "🔔";
        }
    }
}

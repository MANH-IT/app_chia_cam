package com.chupchia.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "groups")
public class Group implements Serializable {
    @PrimaryKey
    @androidx.annotation.NonNull
    private String id;
    private String name;
    private String description;
    private String avatarUrl;
    private String adminId;
    private String adminName;
    private String inviteCode;
    private long createdAt;
    private GroupSettings settings;
    private List<Member> members;
    
    public Group() {
        this.members = new ArrayList<>();
        this.settings = new GroupSettings();
        this.createdAt = System.currentTimeMillis();
    }
    
    @Ignore
    public Group(String id, String name, String description, String avatarUrl, 
                 String adminId, String adminName, String inviteCode) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.avatarUrl = avatarUrl;
        this.adminId = adminId;
        this.adminName = adminName;
        this.inviteCode = inviteCode;
        this.members = new ArrayList<>();
        this.settings = new GroupSettings();
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public GroupSettings getSettings() { return settings; }
    public void setSettings(GroupSettings settings) { this.settings = settings; }
    
    public List<Member> getMembers() { return members; }
    public void setMembers(List<Member> members) { this.members = members; }
    
    public void addMember(Member member) {
        this.members.add(member);
    }
    
    public int getMemberCount() {
        return members.size();
    }
    
    public static class GroupSettings implements Serializable {
        private boolean defaultSplitEnabled;
        private boolean membersCanInvite;
        private boolean autoReminderEnabled;
        
        public GroupSettings() {
            this.defaultSplitEnabled = true;
            this.membersCanInvite = false;
            this.autoReminderEnabled = true;
        }
        
        public GroupSettings(boolean defaultSplitEnabled, boolean membersCanInvite, boolean autoReminderEnabled) {
            this.defaultSplitEnabled = defaultSplitEnabled;
            this.membersCanInvite = membersCanInvite;
            this.autoReminderEnabled = autoReminderEnabled;
        }
        
        public boolean isDefaultSplitEnabled() { return defaultSplitEnabled; }
        public void setDefaultSplitEnabled(boolean defaultSplitEnabled) { this.defaultSplitEnabled = defaultSplitEnabled; }
        
        public boolean isMembersCanInvite() { return membersCanInvite; }
        public void setMembersCanInvite(boolean membersCanInvite) { this.membersCanInvite = membersCanInvite; }
        
        public boolean isAutoReminderEnabled() { return autoReminderEnabled; }
        public void setAutoReminderEnabled(boolean autoReminderEnabled) { this.autoReminderEnabled = autoReminderEnabled; }
    }
}

package com.chupchia.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReactionGroup implements Serializable {
    private String emoji;
    private List<ReactionUser> users;
    
    public ReactionGroup(String emoji) {
        this.emoji = emoji;
        this.users = new ArrayList<>();
    }
    
    public ReactionGroup(String emoji, List<ReactionUser> users) {
        this.emoji = emoji;
        this.users = users;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
    
    public List<ReactionUser> getUsers() {
        return users;
    }
    
    public void setUsers(List<ReactionUser> users) {
        this.users = users;
    }
    
    public void addUser(ReactionUser user) {
        this.users.add(user);
    }
    
    public int getCount() {
        return users.size();
    }
    
    public String getUserNames() {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(users.get(i).getUserName());
        }
        return names.toString();
    }
    
    public static class ReactionUser implements Serializable {
        private String userId;
        private String userName;
        private String avatarUrl;
        
        public ReactionUser(String userId, String userName, String avatarUrl) {
            this.userId = userId;
            this.userName = userName;
            this.avatarUrl = avatarUrl;
        }
        
        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getAvatarUrl() { return avatarUrl; }
    }
}

package com.chupchia.models;

import java.io.Serializable;

public class ReactionItem implements Serializable {
    private String emoji;
    private String name;
    
    public ReactionItem(String emoji, String name) {
        this.emoji = emoji;
        this.name = name;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReactionItem) {
            return emoji.equals(((ReactionItem) obj).getEmoji());
        }
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return emoji != null ? emoji.hashCode() : 0;
    }
}

package com.chupchia.models;

import java.io.Serializable;

public class Contact implements Serializable {
    private String id;
    private String name;
    private String phoneNumber;
    private String avatarUri;
    private boolean isExisting; // Đã dùng Chia Cam
    private boolean isInvited; // Đã được mời
    private boolean isMember; // Đã là thành viên nhóm này
    
    public Contact() {}
    
    public Contact(String name, String phoneNumber, boolean isExisting) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isExisting = isExisting;
        this.isInvited = false;
        this.isMember = false;
    }
    
    public Contact(String id, String name, String phoneNumber, String avatarUri, boolean isExisting) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.avatarUri = avatarUri;
        this.isExisting = isExisting;
        this.isInvited = false;
        this.isMember = false;
    }
    
    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAvatarUri() { return avatarUri; }
    public void setAvatarUri(String avatarUri) { this.avatarUri = avatarUri; }
    
    public boolean isExisting() { return isExisting; }
    public void setExisting(boolean existing) { isExisting = existing; }
    
    public boolean isInvited() { return isInvited; }
    public void setInvited(boolean invited) { isInvited = invited; }
    
    public boolean isMember() { return isMember; }
    public void setMember(boolean member) { isMember = member; }
    
    public String getDisplayPhone() {
        if (phoneNumber == null) return "";
        if (phoneNumber.length() >= 10) {
            return phoneNumber.substring(0, 4) + "***" + phoneNumber.substring(phoneNumber.length() - 3);
        }
        return phoneNumber;
    }
}

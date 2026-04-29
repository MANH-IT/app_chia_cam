package com.chupchia.models;

public class OnboardingSlide {
    private String icon;
    private String title;
    private String description;
    private String subDescription;
    
    public OnboardingSlide(String icon, String title, String description, String subDescription) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.subDescription = subDescription;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSubDescription() {
        return subDescription;
    }
    
    public void setSubDescription(String subDescription) {
        this.subDescription = subDescription;
    }
}

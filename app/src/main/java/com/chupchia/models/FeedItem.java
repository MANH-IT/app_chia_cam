package com.chupchia.models;

public class FeedItem {
    public static final int TYPE_ADD_PHOTO = 0;
    public static final int TYPE_IMAGE = 1;

    private int type;
    private String imageUrl;
    private String amount;
    private String userName;
    private String reaction;

    // Constructor for "Add Photo" item
    public FeedItem() {
        this.type = TYPE_ADD_PHOTO;
    }

    // Constructor for image items
    public FeedItem(String imageUrl, String amount, String userName, String reaction) {
        this.type = TYPE_IMAGE;
        this.imageUrl = imageUrl;
        this.amount = amount;
        this.userName = userName;
        this.reaction = reaction;
    }

    public int getType() { return type; }
    public String getImageUrl() { return imageUrl; }
    public String getAmount() { return amount; }
    public String getUserName() { return userName; }
    public String getReaction() { return reaction; }
}

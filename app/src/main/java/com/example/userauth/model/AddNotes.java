package com.example.userauth.model;

public class AddNotes {
    private String key;
    private String title;
    private String description;
    private String priority;
    private String uriImage;
    private int ProfileImage;

    public AddNotes() {
        // Default constructor required for calls to DataSnapshot.getValue(AddNotes.class)
    }

    public AddNotes(String key, String title, String description, String priority, String uriImage, int ProfileImage) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.uriImage = uriImage;
        this.ProfileImage = ProfileImage;
    }

    // Getters and setters
    public String getKey() {
        return key;}

    public void setKey(String key) {
        this.key = key;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getUriImage() {
        return uriImage;
    }

    public void setUriImage(String uriImage) {
        this.uriImage = uriImage;
    }
    public void setProfileImage(int profileImage) {
        ProfileImage = profileImage;

    }
    public int getProfileImage() {
        return ProfileImage;
    }
}
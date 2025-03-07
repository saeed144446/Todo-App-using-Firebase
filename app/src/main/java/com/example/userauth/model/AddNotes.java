package com.example.userauth.model;

public class AddNotes {
    private String title;
    private String description;
    private String key;
    private String priority;
    private String uriImage;

    public AddNotes() {
    }

    public AddNotes(String title, String description, String key, String priority, String uriImage) {
        this.title = title;
        this.description = description;
        this.key = key;
        this.priority = priority;
        this.uriImage = uriImage;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

}
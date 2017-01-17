package ru.forum.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePostRequest {
    private long thread;
    private String forum;
    private String user;
    private String message;
    private String date;
    private Long parent;
    private Boolean isApproved;
    private Boolean isHighlighted;
    private Boolean isEdited;
    private Boolean isSpam;
    private Boolean isDeleted;

    public long getThread() {
        return thread;
    }

    public String getForum() {
        return forum;
    }

    public String getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    @JsonProperty("parent")
    public long getParent() {
        return parent;
    }

    @JsonProperty("isApproved")
    public boolean isApproved() {
        return isApproved;
    }

    @JsonProperty("isHighlighted")
    public boolean isHighlighted() {
        return isHighlighted;
    }

    @JsonProperty("isEdited")
    public boolean isEdited() {
        return isEdited;
    }

    @JsonProperty("isSpam")
    public boolean isSpam() {
        return isSpam;
    }

    @JsonProperty("isDeleted")
    public boolean isDeleted() {
        return isDeleted;
    }

    @JsonProperty("parent")
    public void setParent(Long parent) {
        this.parent = parent == null ? 0 : parent;
    }

    @JsonProperty("isApproved")
    public void setApproved(Boolean approved) {
        isApproved = approved == null ? false : approved;
    }

    @JsonProperty("isHighlighted")
    public void setHighlighted(Boolean highlighted) {
        isHighlighted = highlighted == null ? false : highlighted;
    }

    @JsonProperty("isEdited")
    public void setEdited(Boolean edited) {
        isEdited = edited == null ? false : edited;
    }

    @JsonProperty("isSpam")
    public void setSpam(Boolean spam) {
        isSpam = spam == null ? false : spam;
    }

    @JsonProperty("isDeleted")
    public void setDeleted(Boolean deleted) {
        isDeleted = deleted == null ? false : deleted;
    }
}

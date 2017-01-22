package ru.forum.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
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
    public Long getParent() {
        return parent;
    }

    @JsonProperty("isApproved")
    public Boolean isApproved() {
        return isApproved;
    }

    @JsonProperty("isHighlighted")
    public Boolean isHighlighted() {
        return isHighlighted;
    }

    @JsonProperty("isEdited")
    public Boolean isEdited() {
        return isEdited;
    }

    @JsonProperty("isSpam")
    public Boolean isSpam() {
        return isSpam;
    }

    @JsonProperty("isDeleted")
    public Boolean isDeleted() {
        return isDeleted;
    }

    @JsonProperty("parent")
    public void setParent(Long parent) {
        this.parent = parent;
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

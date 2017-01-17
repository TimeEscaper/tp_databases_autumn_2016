package ru.forum.model.request;

public class CreatePostRequest {
    private long thread;
    private String forum;
    private String user;
    private String message;
    private String date;
    private long parent = 0;
    private boolean isApproved = false;
    private boolean isHighlighted = false;
    private boolean isEdited = false;
    private boolean isSpam = false;
    private boolean isDeleted = false;

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

    public long getParent() {
        return parent;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}

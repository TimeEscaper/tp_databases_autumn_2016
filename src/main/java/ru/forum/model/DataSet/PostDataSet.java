package ru.forum.model.DataSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
public class PostDataSet {

    private long id;
    private long thread;
    private String forum;
    private String user;
    private String message;
    private String date;
    private long parent;
    private boolean isApproved;
    private boolean isHighlighted;
    private boolean isEdited;
    private boolean isSpam;
    private boolean isDeleted;
    private long likes = 0;

    public PostDataSet(long id, long thread, String forum, String user, String message, String date, long parent,
                       boolean isApproved, boolean isHighlighted, boolean isEdited, boolean isSpam, boolean isDeleted) {
        this.id = id;
        this.thread = thread;
        this.forum = forum;
        this.user = user;
        this.message = message;
        this.date = date;
        this.parent = parent;
        this.isApproved = isApproved;
        this.isHighlighted = isHighlighted;
        this.isEdited = isEdited;
        this.isSpam = isSpam;
        this.isDeleted = isDeleted;
    }

    public PostDataSet(long id, long thread, String forum, String user, String message, String date, long parent,
                       boolean isApproved, boolean isHighlighted, boolean isEdited, boolean isSpam, boolean isDeleted,
                       long likes) {
        this.id = id;
        this.thread = thread;
        this.forum = forum;
        this.user = user;
        this.message = message;
        this.date = date;
        this.parent = parent;
        this.isApproved = isApproved;
        this.isHighlighted = isHighlighted;
        this.isEdited = isEdited;
        this.isSpam = isSpam;
        this.isDeleted = isDeleted;
        this.likes = likes;
    }

    public long getId() {
        return id;
    }

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

    public long getLikes() { return likes; }
}

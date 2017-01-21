package ru.forum.model.full;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class PostFull {

    private long id;
    private Object thread;
    private Object forum;
    private Object user;
    private String message;
    private String date;
    private Long parent;
    private boolean isApproved;
    private boolean isHighlighted;
    private boolean isEdited;
    private boolean isSpam;
    private boolean isDeleted;
    private long likes;
    private long dislikes;
    private long points;

    public PostFull(long id, Object thread, Object forum, Object user, String message, String date, Long parent,
                    boolean isApproved, boolean isHighlighted, boolean isEdited, boolean isSpam, boolean isDeleted,
                    long likes, long dislikes) {
        this.id = id;
        this.thread = thread;
        this.forum = forum;
        this.user = user;
        this.message = message;
        this.date = date.substring(0, 19);
        this.parent = parent == 0 ? null : parent;
        this.isApproved = isApproved;
        this.isHighlighted = isHighlighted;
        this.isEdited = isEdited;
        this.isSpam = isSpam;
        this.isDeleted = isDeleted;
        this.likes = likes;
        this.dislikes = dislikes;
        this.points = likes - dislikes;
    }

    public PostFull(long id, String message, String date, Long parent, boolean isApproved, boolean isHighlighted,
                    boolean isEdited, boolean isSpam, boolean isDeleted, long likes, long dislikes) {
        this.id = id;
        this.message = message;
        this.date = date.substring(0, 19);
        this.parent = parent == 0 ? null : parent;
        this.isApproved = isApproved;
        this.isHighlighted = isHighlighted;
        this.isEdited = isEdited;
        this.isSpam = isSpam;
        this.isDeleted = isDeleted;
        this.likes = likes;
        this.dislikes = dislikes;
        this.points = likes - dislikes;
    }

    public long getId() {
        return id;
    }

    public Object getThread() {
        return thread;
    }

    public Object getForum() {
        return forum;
    }

    public Object getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public Long getParent() {
        return parent;
    }

    @JsonProperty("isApproved")
    public boolean isApproved() {
        return isApproved;
    }

    public void setThread(Object thread) {
        this.thread = thread;
    }

    public void setForum(Object forum) {
        this.forum = forum;
    }

    public void setUser(Object user) {
        this.user = user;
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

    public long getLikes() { return likes; }

    public long getDislikes() { return dislikes; }

    public long getPoints() { return points; }
}

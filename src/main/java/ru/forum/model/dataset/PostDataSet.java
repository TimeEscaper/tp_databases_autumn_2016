package ru.forum.model.dataset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
//@JsonIgnoreProperties({"rootParent", "path"})
public class PostDataSet {

    private long id;
    private long thread;
    private String forum;
    private String user;
    private String message;
    private String date;
    private Long parent;
    private boolean isApproved;
    private boolean isHighlighted;
    private boolean isEdited;
    private boolean isSpam;
    private boolean isDeleted;
    private long likes = 0;
    private long dislikes = 0;
    private long points = 0;
    private long rootParent = 0;
    private String path;

    public PostDataSet(long id, long thread, String forum, String user, String message, String date, Long parent,
                       boolean isApproved, boolean isHighlighted, boolean isEdited, boolean isSpam, boolean isDeleted) {
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
        this.rootParent = id;
    }

    public PostDataSet(long id, long thread, String forum, String user, String message, String date, Long parent,
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
        this.rootParent = id;
    }

    public PostDataSet(long id, long thread, String forum, String user, String message, String date, Long parent,
                       boolean isApproved, boolean isHighlighted, boolean isEdited, boolean isSpam,
                       boolean isDeleted, long likes, long dislikes, long rootParent, String path) {
        this.id = id;
        this.thread = thread;
        this.forum = forum;
        this.user = user;
        this.message = message;
        this.date = date.substring(0, 19);
        this.parent = parent == 0 ? null : parent;;
        this.isApproved = isApproved;
        this.isHighlighted = isHighlighted;
        this.isEdited = isEdited;
        this.isSpam = isSpam;
        this.isDeleted = isDeleted;
        this.likes = likes;
        this.dislikes = dislikes;
        this.points = likes - dislikes;
        this.rootParent = rootParent;
        this.path = path;
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

    public Long getParent() {
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

    public long getLikes() { return likes; }

    public long getDislikes() { return dislikes; }

    public long getPoints() { return points; }

    public long getRootParent() { return rootParent; }

    public String getPath() { return path; }

    public void setId(long id) {
        this.id = id;
    }

    public void setRootParent(long rootParent) {
        this.rootParent = rootParent;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

package ru.forum.model.full;

@SuppressWarnings("unused")
public class PostFull {

    private long id;
    private Object thread;
    private Object forum;
    private Object user;
    private String message;
    private String date;
    private long parent;
    private boolean isApproved;
    private boolean isHighlighted;
    private boolean isEdited;
    private boolean isSpam;
    private boolean isDeleted;
    private long likes;

    public PostFull(long id, Object thread, Object forum, Object user, String message, String date, long parent,
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

    public PostFull(long id, String message, String date, long parent, boolean isApproved, boolean isHighlighted,
                    boolean isEdited, boolean isSpam, boolean isDeleted, long likes) {
        this.id = id;
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

    public long getParent() {
        return parent;
    }

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

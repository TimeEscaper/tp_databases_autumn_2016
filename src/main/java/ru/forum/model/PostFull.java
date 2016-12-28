package ru.forum.model;

@SuppressWarnings("unused")
public class PostFull<T, F, U> {

    private long id;
    private T thread;
    private F forum;
    private U user;
    private String message;
    private String date;
    private long parent;
    private boolean isApproved;
    private boolean isHighlighted;
    private boolean isEdited;
    private boolean isSpam;
    private boolean isDeleted;

    public PostFull(long id, T thread, F forum, U user, String message, String date, long parent,
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

    public PostFull(long id, String message, String date, long parent, boolean isApproved, boolean isHighlighted,
                    boolean isEdited, boolean isSpam, boolean isDeleted) {
        this.id = id;
        this.message = message;
        this.date = date;
        this.parent = parent;
        this.isApproved = isApproved;
        this.isHighlighted = isHighlighted;
        this.isEdited = isEdited;
        this.isSpam = isSpam;
        this.isDeleted = isDeleted;
    }

    public long getId() {
        return id;
    }

    public T getThread() {
        return thread;
    }

    public F getForum() {
        return forum;
    }

    public U getUser() {
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

    public void setThread(T thread) {
        this.thread = thread;
    }

    public void setForum(F forum) {
        this.forum = forum;
    }

    public void setUser(U user) {
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
}

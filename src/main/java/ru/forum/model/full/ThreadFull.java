package ru.forum.model.full;

@SuppressWarnings("unused")
public class ThreadFull {
    long id;
    private Object forum;
    private Object user;
    private String date;
    private String title;
    private String slug;
    private String message;
    private boolean isClosed;
    private boolean isDeleted;
    private long likes;
    private long dislikes;
    private long points;

    public ThreadFull(long id, String date, String title, String slug, String message, boolean isClosed,
                      boolean isDeleted, long likes, long dislikes) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.slug = slug;
        this.message = message;
        this.isClosed = isClosed;
        this.isDeleted = isDeleted;
        this.likes = likes;
        this.dislikes = dislikes;
        this.points = likes - dislikes;
    }

    public ThreadFull(long id, Object forum, Object user, String date, String title, String slug, String message,
                      boolean isClosed, boolean isDeleted, long likes, long dislikes) {
        this.id = id;
        this.forum = forum;
        this.user = user;
        this.date = date;
        this.title = title;
        this.slug = slug;
        this.message = message;
        this.isClosed = isClosed;
        this.isDeleted = isDeleted;
        this.likes = likes;
        this.dislikes = dislikes;
        this.points = likes - dislikes;
    }

    public void setForum(Object forum) {
        this.forum = forum;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public long getId() { return id; }

    public Object getForum() {
        return forum;
    }

    public Object getUser() {
        return user;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getMessage() {
        return message;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public boolean isDeleted() { return isDeleted; }

    public long getLikes() { return likes; }

    public long getDislikes() { return dislikes; }

    public long getPoints() { return points; }
}

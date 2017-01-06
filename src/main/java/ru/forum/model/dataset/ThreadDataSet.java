package ru.forum.model.dataset;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings("unused")
@JsonIgnoreProperties({"vote"})
public class ThreadDataSet {

    long id;
    private String forum;
    private String user;
    private String date;
    private String title;
    private String slug;
    private String message;
    private boolean isClosed;
    private boolean isDeleted;
    private long likes = 0;
    private long dislikes = 0;
    private long points = 0;

    public ThreadDataSet(long id, String forum, String user, String date, String title, String slug, String message,
                         boolean isClosed, boolean isDeleted) {
        this.id = id;
        this.forum = forum;
        this.user = user;
        this.date = date;
        this.title = title;
        this.slug = slug;
        this.message = message;
        this.isClosed = isClosed;
        this.isDeleted = isDeleted;
    }

    public ThreadDataSet(long id, String forum, String user, String date, String title, String slug, String message,
                         boolean isClosed) {
        this.id = id;
        this.forum = forum;
        this.user = user;
        this.date = date;
        this.title = title;
        this.slug = slug;
        this.message = message;
        this.isClosed = isClosed;
    }

    public ThreadDataSet(long id, String forum, String user, String date, String title, String slug, String message,
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

    public long getId() {
        return id;
    }

    public String getForum() { return forum; }

    public String getUser() {
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public long getLikes() { return likes; }

    public long getDislikes() { return dislikes; }

    public long getPoints() { return points; }
}

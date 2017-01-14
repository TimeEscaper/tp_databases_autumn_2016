package ru.forum.model.request;

public class CreateThreadRequest {
    private String forum;
    private String user;
    private String date;
    private String title;
    private String slug;
    private String message;
    private boolean isClosed;
    private boolean isDeleted = false;

    public String getForum() {
        return forum;
    }

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
}

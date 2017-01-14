package ru.forum.model.request;

public class UpdateThreadRequest {
    private long thread;
    private String message;
    private String slug;

    public long getThread() {
        return thread;
    }

    public String getMessage() {
        return message;
    }

    public String getSlug() {
        return slug;
    }
}

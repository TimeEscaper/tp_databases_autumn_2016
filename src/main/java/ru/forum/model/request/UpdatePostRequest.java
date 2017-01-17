package ru.forum.model.request;

public class UpdatePostRequest {
    private long post;
    private String message;

    public long getPost() {
        return post;
    }

    public String getMessage() {
        return message;
    }
}

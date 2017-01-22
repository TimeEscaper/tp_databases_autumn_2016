package ru.forum.model.request;

@SuppressWarnings("unused")
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

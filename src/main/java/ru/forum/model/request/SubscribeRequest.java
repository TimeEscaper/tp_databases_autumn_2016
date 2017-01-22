package ru.forum.model.request;

@SuppressWarnings("unused")
public class SubscribeRequest {
    private long thread;
    private String user;

    public long getThread() {
        return thread;
    }

    public String getUser() {
        return user;
    }
}

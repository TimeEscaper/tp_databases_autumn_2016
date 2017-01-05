package ru.forum.model.dataset;

@SuppressWarnings("unused")
public class SubscriptionDataSet {

    private long thread;
    private String user;

    public SubscriptionDataSet(long thread, String user) {
        this.thread = thread;
        this.user = user;
    }

    public long getThread() {
        return thread;
    }

    public String getUser() {
        return user;
    }
}

package ru.forum.model;

@SuppressWarnings("unused")
public class DbStatus {

    private long user;
    private long thread;
    private long forum;
    private long post;

    public DbStatus(long user, long thread, long forum, long post) {
        this.user = user;
        this.thread = thread;
        this.forum = forum;
        this.post = post;
    }

    public long getUser() { return user; }

    public long getThread() { return thread; }

    public long getForum() { return forum; }

    public long getPost() { return post; }
}

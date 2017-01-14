package ru.forum.model.request;

public class VoteRequest {
    private int vote;
    private long thread;

    public int getVote() {
        return vote;
    }

    public long getThread() {
        return thread;
    }
}

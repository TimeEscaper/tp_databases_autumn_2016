package ru.forum.model.request;

public class VoteThreadRequest {
    private int vote;
    private long thread;

    public int getVote() {
        return vote;
    }

    public long getThread() {
        return thread;
    }
}

package ru.forum.model.request;

@SuppressWarnings("unused")
public class VotePostRequest {
    private long post;
    private int vote;

    public long getPost() {
        return post;
    }

    public int getVote() {
        return vote;
    }
}

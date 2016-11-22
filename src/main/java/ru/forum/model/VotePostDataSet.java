package ru.forum.model;

@SuppressWarnings("unused")
public class VotePostDataSet {

    private long post;
    private short vote;

    public VotePostDataSet(long post, short vote) {
        this.post = post;
        this.vote = vote;
    }

    public long getPost() {
        return post;
    }

    public short getVote() {
        return vote;
    }
}

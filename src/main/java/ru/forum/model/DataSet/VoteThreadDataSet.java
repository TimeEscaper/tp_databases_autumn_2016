package ru.forum.model.DataSet;


@SuppressWarnings("unused")
public class VoteThreadDataSet {

    private long thread;
    private short vote;

    public VoteThreadDataSet(long thread, short vote) {
        this.thread = thread;
        this.vote = vote;
    }

    public long getThread() {
        return thread;
    }

    public short getVote() {
        return vote;
    }
}

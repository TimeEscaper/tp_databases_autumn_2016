package ru.forum.model;


@SuppressWarnings("unused")
public class FollowDataSet {

    private String follower;
    private String followee;

    public FollowDataSet(String follower, String followee) {
        this.follower = follower;
        this.followee = followee;
    }

    public String getFollower() {
        return follower;
    }

    public String getFollowee() {
        return followee;
    }
}

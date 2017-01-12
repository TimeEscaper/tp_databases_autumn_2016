package ru.forum.model.request;

@SuppressWarnings("unused")
public class FollowUserRequest {
    String follower;
    String followee;

    public String getFollower() {
        return follower;
    }

    public String getFollowee() {
        return followee;
    }
}

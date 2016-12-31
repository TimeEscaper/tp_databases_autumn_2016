package ru.forum.model.Full;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class UserFull {

    private long id;
    private String email;
    private String username;
    private String about;
    private String name;
    private boolean isAnonymous;
    private List<String> followers = new ArrayList<>();
    private List<String> following = new ArrayList<>();
    private List<Long> subscriptions = new ArrayList<>();

    public UserFull(long id, String email, String username, String about, String name, boolean isAnonymous,
                    List<String> followers, List<String> following, List<Long> subscriptions) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.about = about;
        this.name = name;
        this.isAnonymous = isAnonymous;
        this.followers = followers;
        this.following = following;
        this.subscriptions = subscriptions;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getAbout() {
        return about;
    }

    public String getName() {
        return name;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public List<Long> getSubscriptions() {
        return subscriptions;
    }
}

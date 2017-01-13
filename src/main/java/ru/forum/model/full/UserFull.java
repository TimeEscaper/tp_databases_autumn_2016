package ru.forum.model.full;

import java.util.ArrayList;
import java.util.Collections;
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
                    String followers, String following, String subscriptions) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.about = about;
        this.name = name;
        this.isAnonymous = isAnonymous;
        if (followers != null) {
            final String[] followersSplit = followers.split(",");
            Collections.addAll(this.followers, followersSplit);
        }
        if (following != null) {
            final String[] followingSplit = following.split(",");
            Collections.addAll(this.followers, followingSplit);
        }
        if (subscriptions != null) {
            final String[] split = subscriptions.split(",");
            for (String str : split)
                this.subscriptions.add(Long.parseLong(str));
        }
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

package ru.forum.model.full;

@SuppressWarnings("unused")
public class UserFull {

    private long id;
    private String email;
    private String username;
    private String about;
    private String name;
    private boolean isAnonymous;
    private String[] followers;
    private String[] following;
    private long[] subscriptions;

    public UserFull(long id, String email, String username, String about, String name, boolean isAnonymous,
                    String[] followers, String[] following, long[] subscriptions) {
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

    public String[] getFollowers() {
        return followers;
    }

    public String[] getFollowing() {
        return following;
    }

    public long[] getSubscriptions() {
        return subscriptions;
    }
}

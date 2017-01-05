package ru.forum.model.full;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class ForumFull {
    private long id;
    private String name;
    private String shortName;
    private Object user;


    public ForumFull(long id, String name, String shortName, Object user) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.user = user;
    }

    public ForumFull(long id, String name, String shortName) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @JsonProperty("short_name")
    public String getShortName() {
        return shortName;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }
}

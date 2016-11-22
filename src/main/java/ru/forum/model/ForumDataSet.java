package ru.forum.model;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class ForumDataSet {

    private long id;
    private String name;
    private String shortName;
    private String user;


    public ForumDataSet(long id, String name, String shortName, String user) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.user = user;
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

    public String getUser() {
        return user;
    }
}

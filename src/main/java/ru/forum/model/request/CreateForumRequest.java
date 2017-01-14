package ru.forum.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateForumRequest {
    String name;
    String shortName;
    String user;

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

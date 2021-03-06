package ru.forum.model.dataset;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class UserDataSet {

    private long id;
    private String email;
    private String username;
    private String about;
    private String name;
    private boolean isAnonymous;


    public UserDataSet(long id, String email, String username, String about, String name, boolean isAnonymous) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.about = about;
        this.name = name;
        this.isAnonymous = isAnonymous;
    }


    public UserDataSet(long id, String email, String username, String about, String name) {
        this.id = id;
        this.email = email;
        this.username = username.equals("null") ? null : username;
        this.about = about.equals("null") ? null : about;
        this.name = name.equals("null") ? null : name;
        this.isAnonymous = false;
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

    @JsonProperty("isAnonymous")
    public boolean isAnonymous() {
        return isAnonymous;
    }
}

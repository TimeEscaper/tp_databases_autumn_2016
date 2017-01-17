package ru.forum.model.request;


import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateUserRequest {
    String username;
    String about;
    String name;
    String email;
    Boolean isAnonymous;

    public String getUsername() {
        return username;
    }

    public String getAbout() {
        return about;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @JsonProperty("isAnonymous")
    public boolean isAnonymous() {
        return isAnonymous;
    }

    @JsonProperty("isAnonymous")
    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = (isAnonymous == null) ? false : isAnonymous;
    }
}

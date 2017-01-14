package ru.forum.model.request;


public class CreateUserRequest {
    String username;
    String about;
    String name;
    String email;
    boolean isAnonymous = false;

    public CreateUserRequest() { }

    public CreateUserRequest(String username, String about, String name, String email) {
        this.username = username;
        this.about = about;
        this.name = name;
        this.email = email;
        this.isAnonymous = false;
    }

    public CreateUserRequest(String username, String about, String name, String email, boolean isAnonymous) {
        this.username = username;
        this.about = about;
        this.name = name;
        this.email = email;
        this.isAnonymous = isAnonymous;
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

    public String getEmail() {
        return email;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }
}

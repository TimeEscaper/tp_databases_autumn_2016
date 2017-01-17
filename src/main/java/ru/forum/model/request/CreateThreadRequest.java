package ru.forum.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ser.impl.IndexedStringListSerializer;

public class CreateThreadRequest {
    private String forum;
    private String user;
    private String date;
    private String title;
    private String slug;
    private String message;
    private Boolean isClosed;
    private Boolean isDeleted;

    public String getForum() {
        return forum;
    }

    public String getUser() {
        return user;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getMessage() {
        return message;
    }

    @JsonProperty("isClosed")
    public Boolean isClosed() {
        return isClosed;
    }

    @JsonProperty("isDeleted")
    public Boolean isDeleted() {
        return isDeleted;
    }

    @JsonProperty("isDeleted")
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted == null ? false : isDeleted;
    }

    @JsonProperty("isClosed")
    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed == null ? false : isClosed;
    }


}

package ru.forum.model;


@SuppressWarnings("unused")
public class Response<T> {

    private int code;
    private T response;

    public Response(int code, T response) {
        this.code = code;
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public T getResponse() {
        return response;
    }
}

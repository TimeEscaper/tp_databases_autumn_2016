package ru.forum.model;


@SuppressWarnings("unused")
public class Response<T> {

    private short code;
    private T response;

    public Response(short code, T response) {
        this.code = code;
        this.response = response;
    }

    public short getCode() {
        return code;
    }

    public T getResponse() {
        return response;
    }
}

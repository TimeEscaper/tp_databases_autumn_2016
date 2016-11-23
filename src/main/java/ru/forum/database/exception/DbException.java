package ru.forum.database.exception;


@SuppressWarnings("unused")
public class DbException extends Exception {
    public DbException(String message, Exception cause) {
        super(message, cause);
    }
}

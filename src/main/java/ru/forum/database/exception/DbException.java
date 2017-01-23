package ru.forum.database.exception;


import java.sql.SQLException;

@SuppressWarnings("unused")
public class DbException extends Exception {
    private int sqlCode = -1;
    public DbException(String message, Exception cause) {
        super(message, cause);
    }
    public DbException (String message, SQLException cause) {
        super(message, cause);
        this.sqlCode = cause.getErrorCode();
        System.out.println(sqlCode);
    }
    public int getSqlCode() { return sqlCode; }
}

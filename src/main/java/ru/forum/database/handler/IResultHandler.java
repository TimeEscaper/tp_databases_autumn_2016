package ru.forum.database.handler;


import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface IResultHandler<T> {
    T handle(ResultSet resultSet) throws SQLException;
}

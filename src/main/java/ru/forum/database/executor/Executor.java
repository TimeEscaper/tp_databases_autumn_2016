package ru.forum.database.executor;


import ru.forum.database.handler.IResultHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings("unused")
public class Executor {

    public<T> T execQuery(Connection connection, String sqlQuery, IResultHandler<T> resultHandler)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sqlQuery);
            try (ResultSet resultSet = statement.getResultSet()) {
                final T result = resultHandler.handle(resultSet);
                resultSet.close();
                statement.close();
                return result;
            }
        }
    }
}

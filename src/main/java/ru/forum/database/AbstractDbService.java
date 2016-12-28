package ru.forum.database;


import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import ru.forum.database.exception.DbException;
import ru.forum.database.executor.Executor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Formatter;
import java.util.Locale;

@SuppressWarnings("unused")
public abstract class AbstractDbService {

    protected DataSource dataSource;

    protected Connection dbConnection;

    protected StringBuilder stringBuilder;
    protected Formatter formatter;
    protected Executor executor;

    public AbstractDbService() throws DbException {
        try {
            dbConnection = DataSourceUtils.getConnection(dataSource);
        }
        catch (CannotGetJdbcConnectionException e) {
            throw new DbException("Unable to connect to database!", e);
        }

        stringBuilder = new StringBuilder();
        formatter = new Formatter(stringBuilder, Locale.US);

        executor = new Executor();
    }

    protected Connection getConnection() {
        return dbConnection;
    }

}

package ru.forum.database;


import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import ru.forum.database.exception.DbException;

import javax.sql.DataSource;
import java.sql.Connection;

@SuppressWarnings("unused")
public abstract class AbstractDbService {

    protected DataSource dataSource;

    private Connection dbConnection;

    public AbstractDbService() throws DbException {
        try {
            dbConnection = DataSourceUtils.getConnection(dataSource);
        }
        catch (CannotGetJdbcConnectionException e) {
            throw new DbException("Unable to connect to database!", e);
        }
    }

    protected Connection getConnection() {
        return dbConnection;
    }
}

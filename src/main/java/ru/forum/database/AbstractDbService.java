package ru.forum.database;


import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import ru.forum.database.exception.DbException;

import javax.sql.DataSource;
import java.sql.Connection;

@SuppressWarnings("unused")
public abstract class AbstractDbService {

    protected DataSource dataSource;

    protected Connection getConnection()  throws DbException {
        try {
            return DataSourceUtils.getConnection(dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DbException("Unable to connect to database!", e);
        }
    }
}

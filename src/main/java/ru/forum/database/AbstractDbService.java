package ru.forum.database;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.forum.database.exception.DbException;
import ru.forum.database.executor.Executor;

import javax.sql.DataSource;
import javax.xml.ws.WebServiceRef;
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
        stringBuilder = new StringBuilder();
        formatter = new Formatter(stringBuilder, Locale.US);
        executor = new Executor();
    }

    protected Connection getConnection() {
        return dbConnection;
    }

}

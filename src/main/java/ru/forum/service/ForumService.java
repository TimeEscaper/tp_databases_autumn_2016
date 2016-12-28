package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.database.executor.Executor;
import ru.forum.model.ForumDataSet;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

@Service
public class ForumService extends AbstractDbService {

    @Autowired
    public ForumService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
    }

    public ForumDataSet createForum(String name, String shortName, String user) throws DbException {
        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {
            formatter.format("INSERT INTO Forum(name, short_name, user) VALUES('%s','%s','%s');",
                    name, shortName, user);
            final Executor executor = new Executor();

            try {
                final int updated = executor.execUpdate(connection, formatter.toString());
                if (updated == 0)
                    return null;

                formatter.format("SELECT * FROM Forum WHERE short_name = '%s';", shortName);
                return executor.execQuery(connection, formatter.toString(), resultSet -> new ForumDataSet(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("short_name"),
                        resultSet.getString("user")
                ));

            } catch (SQLException e) {
                throw new DbException("Unable to create forum!", e);
            }
        }
    }

    //Get forum short info
    public ForumDataSet getForum(String shortName) throws DbException {
        formatter.format("SELECT * FROM Forum WHERE short_name = '%s';", shortName);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> new ForumDataSet(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("short_name"),
                    resultSet.getString("user")
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get forum dataset!", e);
        }
    }

    
}

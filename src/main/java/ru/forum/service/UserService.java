package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.database.executor.Executor;
import ru.forum.model.UserDataSet;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.Locale;

@SuppressWarnings({"unused", "resource"})
@Service
public class UserService extends AbstractDbService {

    @Autowired
    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDataSet createUser(String username, String about, String name, String email,
                                  boolean isAnonymous) throws DbException {

        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {

            formatter.format("INSERT INTO User(email, username, name, about, isAnon " +
                            "VALUES ('%s','%s','%s','%s','%d');",
                    email, username, name, about, isAnonymous ? 1 : 0);

            final Executor executor = new Executor();
            try {
                final int updated = executor.execUpdate(connection, formatter.toString());
                System.out.println(updated);
                formatter.format("SELECT * FROM User WHERE email = '%s'", email);
                return executor.execQuery(connection, formatter.toString(), resultSet -> new UserDataSet(resultSet.getLong("id"),
                        resultSet.getString("email"),
                        resultSet.getString("username"),
                        resultSet.getString("about"),
                        resultSet.getString("name"),
                        resultSet.getBoolean("isAnonymous")));
            } catch (SQLException e) {
                throw new DbException("Unable to add user!", e);
            }
        }

    }

}

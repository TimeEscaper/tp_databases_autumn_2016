package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.database.executor.Executor;
import ru.forum.model.UserDataSet;
import ru.forum.model.UserFull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"unused", "resource", "MalformedFormatString"})
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
                if (updated == 0)
                    return null;

                //System.out.println(updated);
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

    public List<String> getAllFollowers(String email) throws DbException {
        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {

            formatter.format("SELECT follower FROM Follow WHERE followee = '%';", email);
            final Executor executor = new Executor();
            try {
                return executor.execQuery(connection, formatter.toString(), resultSet -> {
                    final List<String> result = new ArrayList<>();
                    while(resultSet.next()) {
                        result.add(resultSet.getString("follower"));
                    }
                    return result;
                });
            } catch (SQLException e) {
                throw new DbException("Unable to get followers!", e);
            }
        }
    }

    public List<String> getAllFollowing(String email) throws DbException {
        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {

            formatter.format("SELECT followee FROM Follow WHERE follower = '%';", email);
            final Executor executor = new Executor();
            try {
                return executor.execQuery(connection, formatter.toString(), resultSet -> {
                    final List<String> result = new ArrayList<>();
                    while(resultSet.next()) {
                        result.add(resultSet.getString("followee"));
                    }
                    return result;
                });
            } catch (SQLException e) {
                throw new DbException("Unable to get followees!", e);
            }
        }
    }

    public List<Long> getAllSubscription(String email) throws DbException {
        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {

            formatter.format("SELECT thread FROM Subscription WHERE user = '%';", email);
            final Executor executor = new Executor();
            try {
                return executor.execQuery(connection, formatter.toString(), resultSet -> {
                    final List<Long> result = new ArrayList<>();
                    while(resultSet.next()) {
                        result.add(resultSet.getLong("thread"));
                    }
                    return result;
                });
            } catch (SQLException e) {
                throw new DbException("Unable to get subscriptions!", e);
            }
        }
    }

    public UserFull getUserDetails(String email) throws DbException {

        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {
            final List<String> followers = getAllFollowers(email);
            final List<String> following = getAllFollowing(email);
            final List<Long> subscriptions = getAllSubscription(email);
            formatter.format("SELECT * FROM User WHERE email = '%s';", email);
            final Executor executor = new Executor();
            try {
                return executor.execQuery(connection, formatter.toString(), resultSet -> new UserFull(
                        resultSet.getLong("id"),
                        resultSet.getString("eamil"),
                        resultSet.getString("username"),
                        resultSet.getString("about"),
                        resultSet.getString("name"),
                        resultSet.getBoolean("isAnonymous"),
                        followers, following, subscriptions));
            } catch (SQLException e) {
                throw new DbException("Unable to get user details!", e);
            }
        }
    }

}

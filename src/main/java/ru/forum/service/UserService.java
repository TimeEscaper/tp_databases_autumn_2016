package ru.forum.service;

import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.database.executor.Executor;
import ru.forum.model.DataSet.UserDataSet;
import ru.forum.model.Full.UserFull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"unused", "resource", "MalformedFormatString"})
@Service
public class UserService extends AbstractDbService {

    public UserService() throws DbException { }

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

        /*final Connection connection = getConnection();
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
        } */
        formatter.format("SELECT User.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions " +
                "FROM User " +
                "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                "LEFT JOIN Followss AS Following ON (User.email = Following.follower)  " +
                "LEFT JOIN Subscriptions AS Subs ON (User.email = Subs.user) " +
                "WHERE User.email='%s' GROUP BY  User.id;", email);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> new UserFull(
                    resultSet.getLong("id"),
                    resultSet.getString("email"),
                    resultSet.getString("username"),
                    resultSet.getString("about"),
                    resultSet.getString("name"),
                    resultSet.getBoolean("isAnonymous"),
                    (String[])resultSet.getArray("followers").getArray(),
                    (String[])resultSet.getArray("followees").getArray(),
                    (long[])resultSet.getArray("subscriptions").getArray()
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get user details!", e);
        }
    }

    public UserFull followUser(String follower, String followee) throws DbException {

        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {
            formatter.format("INSERT INTO Follow (follower, followee) VALUES ('%s', '%s');", follower, followee);
            final Executor executor = new Executor();
            try {
                final int update = executor.execUpdate(connection, formatter.toString());

                return getUserDetails(follower);
            } catch (SQLException e) {
                throw new DbException("Unable to follow user!", e);
            }
        }
    }

    public UserFull unfollowUser(String follower, String followee) throws DbException {

        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {
            formatter.format("DELETE FROM Follow WHERE follower = '%s' AND followee = '%s';", follower, followee);
            final Executor executor = new Executor();
            try {
                final int update = executor.execUpdate(connection, formatter.toString());

                return getUserDetails(follower);
            } catch (SQLException e) {
                throw new DbException("Unable to unfollow user!", e);
            }
        }
    }

    public List<UserFull> getListFollowers(String email, int limit, String order, int sinceId) throws DbException {
        //TODO: переделать на один SELECT
        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {
            if (limit == 0)
                formatter.format("SELECT Follow.follower FROM Follow JOIN User ON (Follow.follower = User.email)" +
                        " WHERE (Follow.followee = '%s') " +
                        "AND (id >= '%d') ORDER BY User.id '%s';", email, sinceId, order);
            else
                formatter.format("SELECT Follow.follower FROM Follow JOIN User ON (Follow.follower = User.email)" +
                        " WHERE (Follow.followee = '%s') " +
                        "AND (id >= '%d') ORDER BY User.id '%s' LIMIT %d;", email, sinceId, order, limit);

            final Executor executor = new Executor();
            try {

                final List<String> followers = executor.execQuery(connection, formatter.toString(), resultSet -> {
                    final List<String> followersArray = new ArrayList<>();
                    while (resultSet.next())
                        followersArray.add(resultSet.getString("follower"));
                    return followersArray;
                });

                final List<UserFull> result = new ArrayList<>();
                for (String follower : followers) {
                    result.add(getUserDetails(follower));
                }

                return result;

            } catch (SQLException e) {
                throw new DbException("Unable to get followers!", e);
            }
        }
    }

    public List<UserFull> getListFollowing(String email, int limit, String order, int sinceId) throws DbException {
        //TODO: переделать на один SELECT
        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {
            if (limit == 0)
                formatter.format("SELECT Follow.followee FROM Follow JOIN User ON (Follow.followee = User.email)" +
                        " WHERE (Follow.follower = '%s') " +
                        "AND (id >= '%d') ORDER BY User.id '%s';", email, sinceId, order);
            else
                formatter.format("SELECT Follow.followee FROM Follow JOIN User ON (Follow.followee = User.email)" +
                        " WHERE (Follow.follower = '%s') " +
                        "AND (id >= '%d') ORDER BY User.id '%s' LIMIT %d;", email, sinceId, order, limit);

            final Executor executor = new Executor();
            try {

                final List<String> followings = executor.execQuery(connection, formatter.toString(), resultSet -> {
                    final List<String> followingsArray = new ArrayList<>();
                    while (resultSet.next())
                        followingsArray.add(resultSet.getString("followee"));
                    return followingsArray;
                });

                final List<UserFull> result = new ArrayList<>();
                for (String following : followings) {
                    result.add(getUserDetails(following));
                }

                return result;

            } catch (SQLException e) {
                throw new DbException("Unable to get followings!", e);
            }
        }
    }

    public UserFull updateUser(String user, String about, String name) throws DbException {
        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {
            formatter.format("UPDATE User (about, name) SET ('%s','%s') WHERE email = '%s';", about, name, user);
            final Executor executor = new Executor();
            try {
                final int update = executor.execUpdate(connection, formatter.toString());

                return getUserDetails(user);
            } catch (SQLException e) {
                throw new DbException("Unable to update profile!", e);
            }
        }
    }

}

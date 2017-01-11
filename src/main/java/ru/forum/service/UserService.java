package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.dataset.UserDataSet;
import ru.forum.model.full.PostFull;
import ru.forum.model.full.UserFull;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "resource", "MalformedFormatString", "Duplicates"})
@Service
public class UserService extends AbstractDbService {

    @Autowired
    public UserService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
        try {
            this.dbConnection = DataSourceUtils.getConnection(this.dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DbException("Unable to get database connection!", e);
        }
    }

    public UserDataSet createUser(String username, String about, String name, String email,
                                  boolean isAnonymous) throws DbException {

        formatter.format("INSERT INTO User(email, username, name, about, isAnon VALUES ('%s','%s','%s','%s','%d');",
                email, username, name, about, isAnonymous ? 1 : 0);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return null;
            formatter.format("SELECT * FROM User WHERE email = '%s'", email);
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> new UserDataSet(
                    resultSet.getLong("id"),
                    resultSet.getString("email"),
                    resultSet.getString("username"),
                    resultSet.getString("about"),
                    resultSet.getString("name"),
                    resultSet.getBoolean("isAnonymous")));
        } catch (SQLException e) {
            throw new DbException("Unable to add user!", e);
        }
    }


    public UserFull getUserDetails(String email) throws DbException {
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
                    (String[]) resultSet.getArray("followers").getArray(),
                    (String[]) resultSet.getArray("followees").getArray(),
                    (long[]) resultSet.getArray("subscriptions").getArray()
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get user details!", e);
        }
    }

    public ArrayList<PostFull> listPosts(String email,
                                         String since, Integer limit, String order) throws DbException {
        String query = "SELECT * FROM Post WHERE user = '" + email + '\'';
        if (since != null) {
            query += " AND date >= " + since;
        }
        if (order == null)
            query += " ORDER BY date desc";
        else
            query += "ORDER BY date " + order;
        if (limit != null)
            query += " LIMIT " + limit.toString();
        query += ";";

        try {
            return executor.execQuery(getConnection(), query,
                    resultSet -> {
                        final ArrayList<PostFull> result = new ArrayList<>();
                        while (resultSet.next()) {
                            final PostFull post = new PostFull(
                                    resultSet.getLong("id"),
                                    resultSet.getString("thread"),
                                    resultSet.getString("forum"),
                                    resultSet.getString("user"),
                                    resultSet.getString("message"),
                                    resultSet.getString("date"),
                                    resultSet.getLong("parent"),
                                    resultSet.getBoolean("isApproved"),
                                    resultSet.getBoolean("isHighlighted"),
                                    resultSet.getBoolean("isEdited"),
                                    resultSet.getBoolean("isSpam"),
                                    resultSet.getBoolean("isDeleted"),
                                    resultSet.getLong("likes"),
                                    resultSet.getLong("dislikes")
                            );
                            result.add(post);
                        }

                        return result;
                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get posts or related data!", e);
        }

    }

    public UserFull followUser(String follower, String followee) throws DbException {
        formatter.format("INSERT INTO Follow (follower, followee) VALUES ('%s', '%s');", follower, followee);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return null;
            return getUserDetails(follower);
        } catch (SQLException e) {
            throw new DbException("Unable to follow user!", e);
        }
    }

    public UserFull unfollowUser(String follower, String followee) throws DbException {
        formatter.format("DELETE FROM Follow WHERE follower = '%s' AND followee = '%s';", follower, followee);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return null;
            return getUserDetails(follower);
        } catch (SQLException e) {
            throw new DbException("Unable to unfollow user!", e);
        }
    }

    //TODO: check query
    public List<UserFull> listFollowers(String email, int limit, String order, int sinceId) throws DbException {
        formatter.format("SELECT User.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions " +
                "FROM User " +
                "JOIN Follow AS UserFollowers ON (UserFollowers.followee = '%s' " +
                "AND User.email = UserFollowers.follower) " +
                "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                "LEFT JOIN Followss AS Following ON (User.email = Following.follower)  " +
                "LEFT JOIN Subscriptions AS Subs ON (User.email = Subs.user) " +
                "GROUP BY  User.id;", email);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> {
                final List<UserFull> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(new UserFull(
                            resultSet.getLong("id"),
                            resultSet.getString("email"),
                            resultSet.getString("username"),
                            resultSet.getString("about"),
                            resultSet.getString("name"),
                            resultSet.getBoolean("isAnonymous"),
                            (String[]) resultSet.getArray("followers").getArray(),
                            (String[]) resultSet.getArray("followees").getArray(),
                            (long[]) resultSet.getArray("subscriptions").getArray()));
                }
                return result;
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get user followers!", e);
        }
    }

    //TODO: check query
    public List<UserFull> listFollowing(String email, int limit, String order, int sinceId) throws DbException {
        formatter.format("SELECT User.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions " +
                "FROM User " +
                "JOIN Follow AS UserFollowees ON (UserFollowers.following = '%s' " +
                "AND User.email = UserFollowers.followee) " +
                "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                "LEFT JOIN Followss AS Following ON (User.email = Following.follower)  " +
                "LEFT JOIN Subscriptions AS Subs ON (User.email = Subs.user) " +
                "GROUP BY  User.id;", email);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> {
                final List<UserFull> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(new UserFull(
                            resultSet.getLong("id"),
                            resultSet.getString("email"),
                            resultSet.getString("username"),
                            resultSet.getString("about"),
                            resultSet.getString("name"),
                            resultSet.getBoolean("isAnonymous"),
                            (String[]) resultSet.getArray("followers").getArray(),
                            (String[]) resultSet.getArray("followees").getArray(),
                            (long[]) resultSet.getArray("subscriptions").getArray()));
                }
                return result;
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get user followees!", e);
        }
    }

    public UserFull updateUser(String user, String about, String name) throws DbException {
        formatter.format("UPDATE User (about, name) SET ('%s','%s') WHERE email = '%s';", about, name, user);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return null;
            return getUserDetails(user);
        } catch (SQLException e) {
            throw new DbException("Unable to update profile!", e);
        }
    }

}

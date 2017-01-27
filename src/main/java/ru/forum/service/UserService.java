package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.dataset.UserDataSet;
import ru.forum.model.full.PostFull;
import ru.forum.model.full.UserFull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static ru.forum.helper.QueryHelper.format;

@SuppressWarnings({"unused", "resource", "MalformedFormatString", "Duplicates"})
@Component
public class UserService extends AbstractDbService {

    private GetService getService;

    @Autowired
    public UserService(DataSource dataSource, GetService getService) throws DbException {
        this.dataSource = dataSource;
        this.getService = getService;
    }


    public UserDataSet createUser(String username, String about, String name, String email,
                                  boolean isAnonymous) throws DbException {

        String query;
        if (isAnonymous)
            query = format("INSERT IGNORE INTO User(email, isAnonymous) VALUES ('%s',1);", email);
        else
            query = format("INSERT IGNORE INTO User(email, username, name, about, isAnonymous) VALUES ('%s','%s','%s','%s',0);",
                    email, username, name, about);
        try (Connection connection = getConnection()) {
            if (executor.execUpdate(connection, query) == 0)
                return null;
            query = format("SELECT * FROM User WHERE email = '%s';", email);
            return executor.execQuery(getConnection(), query, resultSet -> {
                resultSet.next();
                return new UserDataSet(
                        resultSet.getLong("id"),
                        resultSet.getString("email"),
                        resultSet.getString("username"),
                        resultSet.getString("about"),
                        resultSet.getString("name"),
                        resultSet.getBoolean("isAnonymous"));
            });
        } catch (SQLException e) {
            //System.out.println(formatter.toString());
            throw new DbException("Unable to add user!", e);
        }
    }


    public UserFull getUserDetails(String email) throws DbException {
        try {
            return getService.getUserFull(email);
        } catch (SQLException e) {
            throw new DbException("Unable to get user!", e);
        }
    }

    public ArrayList<PostFull> listPosts(String email,
                                         String since, Integer limit, String order) throws DbException {
        String query = "SELECT * FROM Post WHERE user = '" + email + '\'';
        if (since != null) {
            query += " AND date >= '" + since + "\' ";
        }
        if (order == null)
            query += " ORDER BY date desc";
        else
            query += " ORDER BY date " + order;
        if (limit != null)
            query += " LIMIT " + limit.toString();
        query += ";";

        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query,
                    resultSet -> {
                        final ArrayList<PostFull> result = new ArrayList<>();
                        while (resultSet.next()) {
                            final PostFull post = new PostFull(
                                    resultSet.getLong("id"),
                                    resultSet.getLong("thread"),
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

    //TODO: user existance
    public UserFull followUser(String follower, String followee) throws DbException {
        final String query = format("INSERT IGNORE INTO Follow (follower, followee) VALUES ('%s', '%s');", follower, followee);
        try (Connection connection = getConnection()) {
            executor.execUpdate(connection, query);
            return getUserDetails(follower);
        } catch (SQLException e) {
            throw new DbException("Unable to follow user!", e);
        }
    }

    public UserFull unfollowUser(String follower, String followee) throws DbException {
        final String query = format("DELETE IGNORE FROM Follow WHERE follower = '%s' AND followee = '%s';", follower, followee);
        try (Connection connection = getConnection()) {
            executor.execUpdate(connection, query);
            return getUserDetails(follower);
        } catch (SQLException e) {
            throw new DbException("Unable to unfollow user!", e);
        }
    }

    //TODO: check query
    public List<UserFull> listFollowers(String email, Integer limit, String order, Integer sinceId) throws DbException {
        String query = "SELECT User.email FROM User JOIN Follow ON(Follow.followee='"
                 + email + "' AND User.email=Follow.follower) ";
        if (sinceId != null)
            query += " WHERE User.id>=" + sinceId.toString();
        query += " GROUP BY User.id ORDER BY User.name ";
        if (order == null)
            query += "desc ";
        else
            query += order;
        if (limit != null)
            query += " LIMIT " + limit.toString();
        query += ';';
        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query, resultSet -> {
                final List<UserFull> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(getService.getUserFull(resultSet.getString("User.email")));
                }
                return result;
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get user followers!", e);
        }
    }

    //TODO: check query
    public List<UserFull> listFollowing(String email, Integer limit, String order, Integer sinceId) throws DbException {
        String query = "SELECT User.email FROM User JOIN Follow ON(Follow.follower='"
                + email + "' AND User.email=Follow.followee) ";
        if (sinceId != null)
            query += " WHERE User.id >= " + sinceId.toString();
        query += " GROUP BY User.id ORDER BY User.name ";
        if (order == null)
            query += "desc ";
        else
            query += order;
        if (limit != null)
            query += " LIMIT " + limit.toString();
        query += ';';
        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query, resultSet -> {
                final List<UserFull> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(getService.getUserFull(resultSet.getString("User.email")));
                }
                return result;
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get user followees!", e);
        }
    }

    public UserFull updateUser(String user, String about, String name) throws DbException {
        final String query = format("UPDATE IGNORE User SET about='%s', name='%s' WHERE email = '%s';", about, name, user);
        try (Connection connection = getConnection()) {
            if (executor.execUpdate(connection, query) == 0)
                return null;
            return getUserDetails(user);
        } catch (SQLException e) {
            throw new DbException("Unable to update profile!", e);
        }
    }

}

package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.dataset.ForumDataSet;
import ru.forum.model.dataset.PostDataSet;
import ru.forum.model.dataset.SubscriptionDataSet;
import ru.forum.model.dataset.ThreadDataSet;
import ru.forum.model.full.ThreadFull;
import ru.forum.model.full.UserFull;

import javax.jws.soap.SOAPBinding;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;

import static ru.forum.helper.QueryHelper.format;

@SuppressWarnings({"Duplicates", "unused", "OverlyComplexMethod"})
@Component
public class ThreadService extends AbstractDbService {

    UserService userService;

    @Autowired
    public ThreadService(DataSource dataSource, UserService userService) throws DbException {
        this.dataSource = dataSource;
        this.userService = userService;
        try {
            this.dbConnection = DataSourceUtils.getConnection(this.dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DbException("Unable to get database connection!", e);
        }
    }

    public ThreadDataSet createThread(String forum, String title, String user, String date, String message,
                                      String slug, boolean isClosed, boolean isDeleted) throws DbException {

        String query = format("INSERT IGNORE INTO Thread(forum,title,user,date,message,slug,isClosed,isDeleted) " +
                "VALUES('%s','%s','%s','%s','%s','%s','%d','%d');", forum, title, user, date, message, slug,
                isClosed ? 1 : 0, isDeleted ? 1 : 0);
        try {
            final int updated = executor.execUpdate(getConnection(), query);
            if (updated == 0) {
                return null;
            }
        }  catch (SQLException e) {
            throw new DbException("Unable to create thread!", e);
        }

        query = format("SELECT * FROM Thread WHERE forum = '%s' AND title = '%s' AND date = '%s';",
                forum, title, date);
        try {
            return executor.execQuery(getConnection(), query, resultSet -> {
                resultSet.next();
                return new ThreadDataSet(
                        resultSet.getLong("id"),
                        resultSet.getString("forum"),
                        resultSet.getString("user"),
                        resultSet.getString("date"),
                        resultSet.getString("title"),
                        resultSet.getString("slug"),
                        resultSet.getString("message"),
                        resultSet.getBoolean("isClosed"),
                        resultSet.getBoolean("isDeleted")
                );
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get thread!", e);
        }
    }

    public boolean closeThread(long threadId) throws DbException {
        final String query = format("UPDATE IGNORE Thread SET isClosed=1 WHERE id = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), query) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to close thread!", e);
        }
    }

    public ThreadFull threadDetails(long threadId, ArrayList<String> related) throws DbException {

        final boolean containsUser = related.contains("user");
        final boolean containsForum = related.contains("forum");

        String postfix = " WHERE Thread.id = " + Long.toString(threadId);
        postfix += " GROUP BY ";
        String group = " Thread.id;";
        if (containsForum)
            group = " Forum.id, " + group;
        postfix += group;

        final StringBuilder tables = new StringBuilder("SELECT Thread.* ");
        final StringBuilder joins = new StringBuilder(" FROM Thread ");

        if (containsForum) {
            tables.append(" , Forum.* ");
            joins.append(" JOIN Forum ON(Thread.forum = Forum.short_name)");
        }

        final String query = tables.toString() + joins + postfix;
        //System.out.println(query);
        try {
            return executor.execQuery(getConnection(), query,
                    resultSet -> {
                        if (!resultSet.next())
                            return null;
                        //System.out.println(resultSet.getString("Thread.date"));
                        final ThreadFull result = new ThreadFull(
                                resultSet.getLong("Thread.id"),
                                resultSet.getString("Thread.date"),
                                resultSet.getString("Thread.title"),
                                resultSet.getString("Thread.slug"),
                                resultSet.getString("Thread.message"),
                                resultSet.getBoolean("Thread.isClosed"),
                                resultSet.getBoolean("Thread.isDeleted"),
                                resultSet.getLong("Thread.likes"),
                                resultSet.getLong("Thread.dislikes"),
                                resultSet.getLong("Thread.posts")
                        );

                        if (containsUser) {
                            UserFull userFull = userService.getUserFull(resultSet.getString("Thread.user"));
                            result.setUser(userFull);
                        } else {
                            result.setUser(resultSet.getString("Thread.user"));
                        }
                        if (containsForum) {
                            result.setForum(new ForumDataSet(
                                    resultSet.getLong("Forum.id"),
                                    resultSet.getString("Forum.name"),
                                    resultSet.getString("Forum.short_name"),
                                    resultSet.getString("Forum.user")
                            ));
                        } else {
                            result.setForum(resultSet.getString("Thread.forum"));
                        }

                        return result;
                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get thread or related data!", e);
        }
    }

    //TODO: filter creator in utils
    public ArrayList<ThreadDataSet> listThread(String filter, boolean isByUser,
                                                     String since, Integer limit, String order) throws DbException {

        String postfix;
        if (isByUser)
            postfix = " WHERE Thread.user = '" + filter + '\'';
        else
            postfix = " WHERE Thread.forum = '" + filter + '\'';

        if (since != null) {
            postfix += " AND Thread.date >= '" + since + '\'';
        }
        postfix += " GROUP BY Thread.id ";
        if (order == null)
            postfix += " ORDER BY Thread.date desc";
        else
            postfix += " ORDER BY Thread.date " + order;
        if (limit != null)
            postfix += " LIMIT " + limit.toString();
        postfix += ";";

        final String query = "SELECT Thread.* From Thread "  + postfix;
        //System.out.println(query);
        try {
            return executor.execQuery(getConnection(), query, resultSet -> {
                final ArrayList<ThreadDataSet> result = new ArrayList<>();
                while (resultSet.next()) {
                    final ThreadDataSet thread = new ThreadDataSet(
                            resultSet.getLong("id"),
                            resultSet.getString("forum"),
                            resultSet.getString("user"),
                            resultSet.getString("date"),
                            resultSet.getString("title"),
                            resultSet.getString("slug"),
                            resultSet.getString("message"),
                            resultSet.getBoolean("isClosed"),
                            resultSet.getBoolean("isDeleted"),
                            resultSet.getLong("likes"),
                            resultSet.getLong("dislikes"),
                            resultSet.getLong("posts")
                    );
                    result.add(thread);
                }
                return result;
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get threads by user!", e);
        }
    }

    public boolean openThread(long threadId) throws DbException {
        final String query = format("UPDATE IGNORE Thread SET isClosed=0 WHERE id = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), query) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to open thread!", e);
        }
    }

    public boolean removeThread(long threadId) throws DbException {
        String query = format("UPDATE IGNORE Thread SET isDeleted=1, posts=0 WHERE id = %d;", threadId);
        try {
            if (executor.execUpdate(getConnection(), query) == 0)
                return false;
        } catch (SQLException e) {
            throw new DbException("Unable to remove thread!", e);
        }
        query = format("UPDATE IGNORE Post SET isDeleted=1 WHERE thread = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), query) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to remove posts!", e);
        }
    }

    public boolean restoreThread(long threadId) throws DbException {
        String query = format("UPDATE IGNORE Thread SET isDeleted=0, posts=(SELECT COUNT(id) FROM Post " +
                "WHERE thread=%d) WHERE id = %d;", threadId, threadId);
        try {
            if (executor.execUpdate(getConnection(), query) == 0)
                return false;
        } catch (SQLException e) {
            throw new DbException("Unable to restore thread!", e);
        }
        query = format("UPDATE IGNORE Post SET isDeleted=0 WHERE thread = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), query) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to restore posts!", e);
        }
    }

    //TODO: check if already subscribe (through DB schema or through query)
    public SubscriptionDataSet subscribeThread(String userId, long threadId) throws DbException {
        final String query = format("INSERT IGNORE INTO Subscription(thread, user) VALUES(%d, '%s');", threadId, userId);
        try {
            if (executor.execUpdate(getConnection(), query) == 0) {
                return null;
            }
            return new SubscriptionDataSet(threadId, userId);
        } catch (SQLException e) {
            throw new DbException("Unable to subscribe user!", e);
        }
    }

    public SubscriptionDataSet unsubscribeThread(String userId, long threadId) throws DbException {
        final String query = format("DELETE IGNORE From Subscription WHERE user = '%s' AND thread = %d;", userId, threadId);
        try {
            if (executor.execUpdate(getConnection(), query) == 0) {
                return null;
            }
            return new SubscriptionDataSet(threadId, userId);
        } catch (SQLException e) {
            throw new DbException("Unable to unsubscribe user!", e);
        }
    }

    public ThreadDataSet updateThread(long threadId, String slug, String message) throws DbException {
        String query = format("UPDATE IGNORE Thread SET slug='%s', message='%s' WHERE id = %d;", slug, message, threadId);
        try {
            if (executor.execUpdate(getConnection(), query) == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("Unable to update thread!", e);
        }
        query = format("SELECT * FROM Thread WHERE Thread.id = %d;", threadId);
        try {
            return executor.execQuery(getConnection(), query, resultSet -> {
                resultSet.next();
                return new ThreadDataSet(
                        resultSet.getLong("id"),
                        resultSet.getString("forum"),
                        resultSet.getString("user"),
                        resultSet.getString("date"),
                        resultSet.getString("title"),
                        resultSet.getString("slug"),
                        resultSet.getString("message"),
                        resultSet.getBoolean("isClosed"),
                        resultSet.getBoolean("isDeleted"),
                        resultSet.getLong("likes"),
                        resultSet.getLong("dislikes"),
                        resultSet.getLong("posts")
                );
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get thread after update!", e);
        }
    }

    public ThreadDataSet voteThread(long threadId, int vote) throws DbException {
        String query;
        if (vote == 1) {
            query = format("UPDATE IGNORE Thread SET likes = likes + 1 WHERE id = %d;", threadId);
        }
        else if (vote == -1) {
            query = format("UPDATE IGNORE Thread SET dislikes = dislikes + 1 WHERE id = %d;", threadId);
        }
        else
            return null;
        try {
            if (executor.execUpdate(getConnection(), query) == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("Unable to update vote for thread!", e);
        }
        query = format("SELECT * FROM Thread WHERE Thread.id = %d;", threadId);
        try {
            return executor.execQuery(getConnection(), query, resultSet -> {
                resultSet.next();
                return new ThreadDataSet(
                        resultSet.getLong("id"),
                        resultSet.getString("forum"),
                        resultSet.getString("user"),
                        resultSet.getString("date"),
                        resultSet.getString("title"),
                        resultSet.getString("slug"),
                        resultSet.getString("message"),
                        resultSet.getBoolean("isClosed"),
                        resultSet.getBoolean("isDeleted"),
                        resultSet.getLong("likes"),
                        resultSet.getLong("dislikes"),
                        resultSet.getLong("posts")
                );
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get thread after vote update!", e);
        }

    }

    public ArrayList<PostDataSet> listPosts(long threadId, String since, Integer limit, String order, String sort)
        throws DbException {

        if ((sort == null) || (sort.equals("flat"))) {
            String query = "SELECT * FROM Post WHERE thread = " + Long.toString(threadId);
            if (since != null)
                query += " AND date >= '" + since + '\'';
            query += " ORDER BY date " + ((order == null) ? "DESC" : order);
            if (limit != null)
                query += " LIMIT " + limit.toString();
            query += ';';
            try {
                return executor.execQuery(getConnection(), query, resultSet -> {
                    final ArrayList<PostDataSet> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(new PostDataSet(
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
                                resultSet.getLong("dislikes"))
                        );
                    }
                    return result;
                });
            } catch (SQLException e) {
                throw new DbException("Unable to list posts!", e);
            }
        } else if (sort.equals("tree")) {
            String query = "SELECT * FROM Post WHERE thread = " + Long.toString(threadId);
            if (since != null)
                query += " AND date >= '" + since + '\'';
            query += " ORDER BY root_parent " + ((order == null) ? "DESC" : order) + ", path ASC";
            if (limit != null)
                query += " LIMIT " + limit.toString();
            query += ';';
            try {
                return executor.execQuery(getConnection(), query, resultSet -> {
                    final ArrayList<PostDataSet> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(new PostDataSet(
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
                                resultSet.getLong("dislikes"),
                                resultSet.getLong("root_parent"),
                                resultSet.getString("path"))
                        );
                    }
                    return result;
                });
            } catch (SQLException e) {
                throw new DbException("Unable to list posts!", e);
            }
        } else if (sort.equals("parent_tree")) {
            String query = "SELECT * FROM Post WHERE thread = " + Long.toString(threadId);
            if (since != null)
                query += " AND date >= '" + since + '\'';
            query += " ORDER BY root_parent " + ((order == null) ? "DESC" : order) + ", path;";
            try {
                return executor.execQuery(getConnection(), query, resultSet -> {
                    final ArrayList<PostDataSet> result = new ArrayList<>();
                    if (limit == null) {
                        while (resultSet.next()) {
                            result.add(new PostDataSet(
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
                                    resultSet.getLong("dislikes"),
                                    resultSet.getLong("root_parent"),
                                    resultSet.getString("path"))
                            );
                        }
                    } else {
                        int parentCount = 0;
                        int currentParent = -1;
                        while (resultSet.next()) {
                            if (resultSet.getInt("root_parent") != currentParent) {
                                parentCount++;
                                currentParent = resultSet.getInt("root_parent");
                            }
                            if (parentCount > limit)
                                break;
                            result.add(new PostDataSet(
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
                                    resultSet.getLong("dislikes"),
                                    resultSet.getLong("root_parent"),
                                    resultSet.getString("path"))
                            );
                        }
                    }
                    return result;
                });
            } catch (SQLException e) {
                throw new DbException("Unable to list posts!", e);
            }
        }

        return new ArrayList<>();
    }

}

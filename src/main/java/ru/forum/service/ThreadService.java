package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.dataset.ForumDataSet;
import ru.forum.model.dataset.PostDataSet;
import ru.forum.model.dataset.SubscriptionDataSet;
import ru.forum.model.dataset.ThreadDataSet;
import ru.forum.model.full.PostFull;
import ru.forum.model.full.ThreadFull;
import ru.forum.model.full.UserFull;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"Duplicates", "unused"})
@Service
public class ThreadService extends AbstractDbService {

    @Autowired
    public ThreadService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
        try {
            this.dbConnection = DataSourceUtils.getConnection(this.dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DbException("Unable to get database connection!", e);
        }
    }

    public ThreadDataSet createThread(String forum, String title, String user, String date, String message,
                                      String slug, boolean isClosed, boolean isDeleted) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("INSERT INTO Thread(forum,title,user,date,message,slug,isClosed,isDeleted) " +
                "VALUES('%s','%s','%s','%s','%s','%s','%d','%d');", forum, title, user, date, message, slug,
                isClosed ? 1 : 0, isDeleted ? 1 : 0);
        try {
            final int updated = executor.execUpdate(getConnection(), formatter.toString());
            if (updated == 0) {
                return null;
            }
        }  catch (SQLException e) {
            throw new DbException("Unable to create thread!", e);
        }

        stringBuilder.setLength(0);
        formatter.format("SELECT * FROM Thread WHERE forum = '%s' AND title = '%s' AND date = '%s';",
                forum, title, date);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> {
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
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Thread SET isClosed=1 WHERE id = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to close thread!", e);
        }
    }

    public ThreadFull threadDetails(long threadId, ArrayList<String> related) throws DbException {

        final boolean containsUser = related.contains("user");
        final boolean containsForum = related.contains("forum");

        String postfix = " WHERE Thread.id = " + Long.toString(threadId);
        if (containsUser)
            postfix += " GROUP BY User.id";
        if (containsUser && containsForum)
            postfix += " ,Forum.id";
        postfix += ';';

        final StringBuilder tables = new StringBuilder("SELECT Thread.*, COUNT(Tpost.id) AS posts ");
        final StringBuilder joins = new StringBuilder("FROM Thread LEFT JOIN Post AS Tpost ON(Thread.id=Tpost.thread AND Tpost.isDeleted=0)");

        if (containsUser) {
            tables.append(" , User.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                    "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                    "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions ");
            joins.append(" JOIN User ON(Thread.user = User.email) " +
                    "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                    "LEFT JOIN Follow AS Following ON (User.email = Following.follower)  " +
                    "LEFT JOIN Subscription AS Subs ON (User.email = Subs.user) ");
        }
        if (containsForum) {
            tables.append(" , Forum.* ");
            joins.append(" JOIN Forum ON(Thread.forum = Forum.short_name)");
        }

        final String query = tables.toString() + joins + postfix;
        System.out.println(query);
        try {
            return executor.execQuery(getConnection(), query,
                    resultSet -> {
                        if (!resultSet.next())
                            return null;
                        System.out.println(resultSet.getString("Thread.date"));
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
                                resultSet.getLong("posts")
                        );

                        if (containsUser) {
                            result.setUser(new UserFull(
                                    resultSet.getLong("User.id"),
                                    resultSet.getString("User.email"),
                                    resultSet.getString("User.username"),
                                    resultSet.getString("User.about"),
                                    resultSet.getString("User.name"),
                                    resultSet.getBoolean("User.isAnonymous"),
                                    resultSet.getString("followers"),
                                    resultSet.getString("followees"),
                                    resultSet.getString("subscriptions")
                            ));
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

        final String query = "SELECT Thread.*, COUNT(Tpost.id) AS posts FROM Thread LEFT JOIN Post AS Tpost " +
                "ON(Tpost.thread=Thread.id AND Tpost.isDeleted=0) " + postfix;
        System.out.println(query);
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
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Thread SET isClosed=0 WHERE id = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to open thread!", e);
        }
    }

    public boolean removeThread(long threadId) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Thread SET isDeleted=1 WHERE id = %d;", threadId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return false;
        } catch (SQLException e) {
            throw new DbException("Unable to remove thread!", e);
        }
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Post SET isDeleted=1 WHERE thread = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to remove posts!", e);
        }
    }

    public boolean restoreThread(long threadId) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Thread SET isDeleted=0 WHERE id = %d;", threadId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return false;
        } catch (SQLException e) {
            throw new DbException("Unable to restore thread!", e);
        }
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Post SET isDeleted=0 WHERE thread = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to restore posts!", e);
        }
    }

    //TODO: check if already subscribe (through DB schema or through query)
    public SubscriptionDataSet subscribeThread(String userId, long threadId) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("INSERT IGNORE INTO Subscription(thread, user) VALUES(%d, '%s');", threadId, userId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return null;
            }
            return new SubscriptionDataSet(threadId, userId);
        } catch (SQLException e) {
            throw new DbException("Unable to subscribe user!", e);
        }
    }

    public SubscriptionDataSet unsubscribeThread(String userId, long threadId) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("DELETE IGNORE From Subscription WHERE user = '%s' AND thread = %d;", userId, threadId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return null;
            }
            return new SubscriptionDataSet(threadId, userId);
        } catch (SQLException e) {
            throw new DbException("Unable to unsubscribe user!", e);
        }
    }

    public ThreadDataSet updateThread(long threadId, String slug, String message) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Thread SET slug='%s', message='%s' WHERE id = %d;", slug, message, threadId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("Unable to update thread!", e);
        }
        formatter.format("SELECT Thread.*, COUNT(Tpost.id) AS posts FROM Thread LEFT JOIN Post AS Tpost " +
                "ON(Thread.id=Tpost.thread AND Tpost.isDeleted=0) WHERE id = %d;", threadId);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> {
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
        stringBuilder.setLength(0);
        if (vote == 1) {
            formatter.format("UPDATE IGNORE Thread SET likes = likes + 1 WHERE id = %d;", threadId);
        }
        else if (vote == -1) {
            formatter.format("UPDATE IGNORE Thread SET dislikes = dislikes + 1 WHERE id = %d;", threadId);
        }
        else
            return null;
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("Unable to update vote for thread!", e);
        }
        stringBuilder.setLength(0);
        formatter.format("SELECT Thread.*, COUNT(Tpost.id) AS posts FROM Thread LEFT JOIN Post AS Tpost " +
                "ON(Thread.id=Tpost.thread AND Tpost.isDeleted=0) WHERE id = %d;", threadId);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> {
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

        /*String query = "SELECT * FORM Post WHERE thread = " + Long.toString(threadId);
        if (since != null)
            query += " AND date >= " + since;
        if (sort == null)
            sort = "flat";
        if (sort.equals("flat"))
            query += " ORDER BY date " + ((order == null) ? "DESC" : sort); */

        if ((sort == null) || (sort.equals("flat"))) {
            String query = "SELECT * FROM Post WHERE thread = " + Long.toString(threadId);
            if (since != null)
                query += " AND date >= '" + since + '\'';
            if (sort == null)
                sort = "flat";
            if (sort.equals("flat"))
                query += " ORDER BY date " + ((order == null) ? "DESC" : order);
            if (limit != null)
                query += " LIMIT " + limit.toString();
            query += ';';
            try {
                return executor.execQuery(getConnection(), query, resultSet -> {
                    final ArrayList<PostDataSet> result = new ArrayList<PostDataSet>();
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
        }

        return new ArrayList<PostDataSet>();
    }

}

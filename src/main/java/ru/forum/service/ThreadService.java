package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.dataset.ForumDataSet;
import ru.forum.model.dataset.SubscriptionDataSet;
import ru.forum.model.dataset.ThreadDataSet;
import ru.forum.model.full.ThreadFull;
import ru.forum.model.full.UserFull;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;

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
        formatter.format("UPDATE Thread (isClosed) SET (1) WHERE id = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to close thread!", e);
        }
    }

    public ThreadFull threadDetails(long threadId, String user, String forum) throws DbException {

        String postfix = " WHERE Thread.id = " + Long.toString(threadId);
        if (user != null)
            postfix += " GROUP BY User.id";
        postfix += ';';

        final StringBuilder tables = new StringBuilder("SELECT Thread.*, COUNT(Tpost.*) AS posts");
        final StringBuilder joins = new StringBuilder("FROM Thread JOIN Post AS Tpost ON(Thread.id=Tpost.thread)");

        if (user != null) {
            tables.append(" , User.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                    "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                    "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions ");
            joins.append(" JOIN User ON(Thread.user = User.email) " +
                    "JOIN Follow AS UserFollowees ON (UserFollowers.following = '%s' " +
                    "AND User.email = UserFollowers.followee) " +
                    "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                    "LEFT JOIN Followss AS Following ON (User.email = Following.follower)  " +
                    "LEFT JOIN Subscriptions AS Subs ON (User.email = Subs.user) ");
        }
        if (forum != null) {
            tables.append(" , forum.*");
            joins.append(" JOIN Forum ON(Thread.forum = Forum.short_name)");
        }

        final String query = tables.toString() + joins + postfix;

        try {
            return executor.execQuery(getConnection(), query,
                    resultSet -> {
                        resultSet.next();
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

                        if (user != null) {
                            result.setUser(new UserFull(
                                    resultSet.getLong("User.id"),
                                    resultSet.getString("User.email"),
                                    resultSet.getString("User.username"),
                                    resultSet.getString("User.about"),
                                    resultSet.getString("User.name"),
                                    resultSet.getBoolean("User.isAnonymous"),
                                    (String[]) resultSet.getArray("followers").getArray(),
                                    (String[]) resultSet.getArray("followees").getArray(),
                                    (long[]) resultSet.getArray("subscriptions").getArray()
                            ));
                        } else {
                            result.setUser(resultSet.getString("Forum.user"));
                        }
                        if (forum != null) {
                            result.setForum(new ForumDataSet(
                                    resultSet.getLong("Forum.id"),
                                    resultSet.getString("Forum.name"),
                                    resultSet.getString("Forum.shortName"),
                                    resultSet.getString("Forum.user")
                            ));
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
            postfix += " AND Thread.date >= " + since;
        }
        if (order == null)
            postfix += " ORDER BY Thread.date desc";
        else
            postfix += "ORDER BY Thread.date " + order;
        if (limit != null)
            postfix += " LIMIT " + limit.toString();
        postfix += ";";

        final String query = "SELECT Thread.*, COUNT(Tpost.*) AS posts FROM Thread JOIN Post AS Tpost " +
                "ON(Thread.id=Tpost.thread) " + postfix;

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
        formatter.format("UPDATE Thread (isClosed) SET (0) WHERE id = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to open thread!", e);
        }
    }

    public boolean removeThread(long threadId) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("UPDATE Thread (isDeleted) SET (1) WHERE id = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to remove thread!", e);
        }
    }

    public boolean restoreThread(long threadId) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("UPDATE Thread (isDeleted) SET (0) WHERE id = %d;", threadId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to restore thread!", e);
        }
    }

    //TODO: check if already subscribe (through DB schema or through query)
    public SubscriptionDataSet subscribeThread(String userId, long threadId) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("INSERT INTO Subscription(thread, user) VALUES(%d, '%s');", threadId, userId);
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
        formatter.format("DELETE From Subscription WHERE user = '%s' AND thread = %d;", userId, threadId);
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
        formatter.format("UPDATE Thread(slug,message) SET('%s','%s') WHERE id = %d;", slug, message, threadId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("Unable to update thread!", e);
        }
        formatter.format("SELECT Thread.*, COUNT(Tpost.*) AS posts FROM Thread JOIN Post AS Tpost " +
                "ON(Thread.id=Tpost.thread) WHERE id = %d;", threadId);
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

    public ThreadDataSet voteThread(long threadId, short vote) throws DbException {
        stringBuilder.setLength(0);
        if (vote == 1) {
            formatter.format("UPDATE Thread SET likes = likes + 1 WHERE id = %d;", threadId);
        }
        else if (vote == -1) {
            formatter.format("UPDATE Thread SET dislikes = dislikes + 1 WHERE id = %d;", threadId);
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
        formatter.format("SELECT Thread.*, COUNT(Tpost.*) AS posts FROM Thread JOIN Post AS Tpost " +
                "ON(Thread.id=Tpost.thread) WHERE id = %d;", threadId);
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

}

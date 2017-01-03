package ru.forum.service;

import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.DataSet.ForumDataSet;
import ru.forum.model.DataSet.ThreadDataSet;
import ru.forum.model.DataSet.UserDataSet;
import ru.forum.model.Full.PostFull;
import ru.forum.model.Full.ThreadFull;
import ru.forum.model.Full.UserFull;

import java.sql.SQLException;
import java.util.ArrayList;

@SuppressWarnings({"unused", "OverlyComplexMethod"})
@Service
public class ForumService extends AbstractDbService {

    public ForumService() throws DbException {
    }

    public ForumDataSet createForum(String name, String shortName, String user) throws DbException {
        formatter.format("INSERT INTO Forum(name, short_name, user) VALUES('%s','%s','%s');",
                name, shortName, user);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return null;
        } catch (SQLException e) {
            throw new DbException("Unable to create forum!", e);
        }

        formatter.format("SELECT * FROM Forum WHERE short_name = '%s';", shortName);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> new ForumDataSet(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("short_name"),
                    resultSet.getString("user")
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get forum after create!", e);
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

    //TODO: check field names and cast
    @SuppressWarnings("SwitchStatementWithoutDefaultBranch")
    public ArrayList<PostFull> listPosts(String forum,
                                         String since, Integer limit, String order, ArrayList<String> related)
            throws DbException {
        String postfix = " WHERE Post.forum = '" + forum + '\'';
        if (since != null) {
            postfix += " AND Post.date >= " + since;
        }
        if (related.contains("user"))
            postfix += " GROUP BY User.id ";
        if (order == null)
            postfix += " ORDER BY Post.date desc";
        else
            postfix += "ORDER BY Post.date " + order;
        if (limit != null)
            postfix += " LIMIT " + limit.toString();
        postfix += ";";

        final StringBuilder tables = new StringBuilder("SELECT Post.*");
        final StringBuilder joins = new StringBuilder("FROM Post");

        for (String table : related) {
            switch (table) {
                case "forum":
                    tables.append(" , forum.*");
                    joins.append(" JOIN Forum ON(Post.forum = Forum.short_name)");
                    break;
                case "thread":
                    tables.append(" , Thread.*");
                    joins.append(" JOIN Thread ON(Post.thread = Thread.id)");
                    break;
                case "user":
                    tables.append(" , Users.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers," +
                            "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                            "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions ");
                    joins.append(" JOIN User ON(Post.user = User.email)" +
                            "JOIN Follow AS UserFollowees ON (UserFollowers.following = '%s' " +
                            "AND User.email = UserFollowers.followee) " +
                            "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                            "LEFT JOIN Followss AS Following ON (User.email = Following.follower)  " +
                            "LEFT JOIN Subscriptions AS Subs ON (User.email = Subs.user) ");
                    break;
            }
        }

        final String query = tables.toString() + joins + postfix;

        try {
            return executor.execQuery(getConnection(), query,
                    resultSet -> {
                        final ArrayList<PostFull> result = new ArrayList<>();
                        while (resultSet.next()) {
                            final PostFull post = new PostFull(
                                    resultSet.getLong("Post.id"),
                                    resultSet.getString("Post.message"),
                                    resultSet.getString("Post.date"),
                                    resultSet.getLong("Post.parent"),
                                    resultSet.getBoolean("Post.isApproved"),
                                    resultSet.getBoolean("Post.isHighlighted"),
                                    resultSet.getBoolean("Post.isEdited"),
                                    resultSet.getBoolean("Post.isSpam"),
                                    resultSet.getBoolean("Post.isDeleted")

                            );

                            if (related.contains("user")) {
                                post.setUser(new UserFull(
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
                                post.setUser(resultSet.getString("Post.user"));
                            }
                            if (related.contains("forum")) {
                                post.setForum(new ForumDataSet(
                                        resultSet.getLong("Forum.id"),
                                        resultSet.getString("Forum.name"),
                                        resultSet.getString("Forum.shortName"),
                                        resultSet.getString("Forum.user")
                                ));
                            } else {
                                post.setForum(resultSet.getString("Post.forum"));
                            }
                            if (related.contains("thread")) {
                                post.setThread(new ThreadDataSet(
                                        resultSet.getLong("Thread.id"),
                                        resultSet.getString("Thread.forum"),
                                        resultSet.getString("Thread.user"),
                                        resultSet.getString("Thread.date"),
                                        resultSet.getString("Thread.title"),
                                        resultSet.getString("Thread.slug"),
                                        resultSet.getString("Thread.message"),
                                        resultSet.getBoolean("Thread.isClosed"),
                                        resultSet.getBoolean("Thread.isDeleted")
                                ));
                            } else {
                                post.setThread(resultSet.getString("Post.thread"));
                            }

                            result.add(post);
                        }

                        return result;
                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get posts or related data!", e);
        }
    }

    public ArrayList<ThreadFull> listThreads(String forum,
                                             String since, Integer limit, String order, ArrayList<String> related)
            throws DbException {

        String postfix = " WHERE Thread.forum = '" + forum + '\'';
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


        final StringBuilder tables = new StringBuilder("SELECT Thread.*");
        final StringBuilder joins = new StringBuilder("FROM Thread");

        for (String table : related) {
            if (table.equals("forum")) {
                tables.append(" , forum.*");
                joins.append(" JOIN Forum ON(Thread.forum = Forum.short_name)");
            } else if (table.equals("user")) {
                tables.append(" , User.*");
                joins.append(" JOIN User ON(Thread.user = User.email)");
            }
        }

        final String query = tables.toString() + joins + postfix;

        try {
            return executor.execQuery(getConnection(), query,
                    resultSet -> {
                        final ArrayList<ThreadFull> result = new ArrayList<>();
                        while (resultSet.next()) {
                            final ThreadFull thread = new ThreadFull(
                                    resultSet.getLong("Thred.id"),
                                    resultSet.getString("Thread.date"),
                                    resultSet.getString("Thread.title"),
                                    resultSet.getString("Thread.slug"),
                                    resultSet.getString("Thread.message"),
                                    resultSet.getBoolean("Thread.isClosed"),
                                    resultSet.getBoolean("Thread.isDeleted")
                            );

                            if (related.contains("user")) {
                                thread.setUser(new UserDataSet(
                                        resultSet.getLong("User.id"),
                                        resultSet.getString("User.email"),
                                        resultSet.getString("User.username"),
                                        resultSet.getString("User.about"),
                                        resultSet.getString("User.name"),
                                        resultSet.getBoolean("User.isAnonymous")
                                ));
                            } else {
                                thread.setUser(resultSet.getString("Post.user"));
                            }
                            if (related.contains("forum")) {
                                thread.setForum(new ForumDataSet(
                                        resultSet.getLong("Forum.id"),
                                        resultSet.getString("Forum.name"),
                                        resultSet.getString("Forum.shortName"),
                                        resultSet.getString("Forum.user")
                                ));
                            } else {
                                thread.setForum(resultSet.getString("Post.forum"));
                            }

                            result.add(thread);
                        }

                        return result;
                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get threads or related data!", e);
        }
    }


}

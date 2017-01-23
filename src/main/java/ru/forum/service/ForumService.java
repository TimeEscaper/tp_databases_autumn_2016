package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.helper.QueryHelper;
import ru.forum.model.dataset.ForumDataSet;
import ru.forum.model.dataset.ThreadDataSet;
import ru.forum.model.full.ForumFull;
import ru.forum.model.full.PostFull;
import ru.forum.model.full.ThreadFull;
import ru.forum.model.full.UserFull;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;

@SuppressWarnings({"unused", "OverlyComplexMethod", "Duplicates"})
@Service
public class ForumService extends AbstractDbService {

    @Autowired
    public ForumService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
        try {
            this.dbConnection = DataSourceUtils.getConnection(this.dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DbException("Unable to get database connection!", e);
        }
    }

    public ForumDataSet createForum(String name, String shortName, String user) throws DbException {
        String query = QueryHelper.format("INSERT IGNORE INTO Forum(name, short_name, user) VALUES('%s','%s','%s');",
                name, shortName, user);
        //System.out.println(formatter.toString());
        try {
            if (executor.execUpdate(getConnection(), query) == 0)
                return null;
        } catch (SQLException e) {
            //System.out.println(formatter.toString());
            throw new DbException("Unable to create forum!", e);
        }

        query = QueryHelper.format("SELECT * FROM Forum WHERE short_name = '%s';", shortName);
        try {
            return executor.execQuery(getConnection(), query, resultSet -> {
                        resultSet.next();
                        return new ForumDataSet(
                                resultSet.getLong("id"),
                                resultSet.getString("name"),
                                resultSet.getString("short_name"),
                                resultSet.getString("user"));
                    }
            );
        } catch (SQLException e) {
            //System.out.println(formatter.toString());
            throw new DbException("Unable to get forum after create!", e);
        }
    }

    //Get forum short info
    public ForumDataSet getForum(String shortName) throws DbException {
        final String query = QueryHelper.format("SELECT * FROM Forum WHERE short_name = '%s';", shortName);
        try {
            return executor.execQuery(getConnection(), query, resultSet -> {
                resultSet.next();
                return new ForumDataSet(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("short_name"),
                        resultSet.getString("user"));
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get forum dataset!", e);
        }
    }

    public ForumFull forumDetails(String forum, String user) throws DbException {

        String postfix = " WHERE Forum.short_name = '" + forum + '\'';
        if (user != null)
            postfix += " GROUP BY User.id, Forum.id";
        postfix += ';';

        final StringBuilder tables = new StringBuilder("SELECT Forum.* ");
        final StringBuilder joins = new StringBuilder("FROM Forum");

        if (user != null) {
            tables.append(" , User.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                    "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                    "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions ");
            joins.append(" JOIN User ON(Forum.user = User.email) " +
                    "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                    "LEFT JOIN Follow AS Following ON (User.email = Following.follower)  " +
                    "LEFT JOIN Subscription AS Subs ON (User.email = Subs.user) ");
        }

        final String query = tables.toString() + joins + postfix;
        //System.out.println(query);
        try {
            return executor.execQuery(getConnection(), query,
                    resultSet -> {
                        if (!resultSet.next())
                            return null;
                        //System.out.println(resultSet.getString("Forum.name"));
                        final ForumFull result = new ForumFull(
                                resultSet.getLong("Forum.id"),
                                resultSet.getString("Forum.name"),
                                resultSet.getString("Forum.short_name")
                        );

                        if (user != null) {
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
                            result.setUser(resultSet.getString("Forum.user"));
                        }
                        return result;
                    });
        } catch (SQLException e) {
            //System.out.println(query);
            throw new DbException("Unable to get forum or related data!", e);
        }

    }

    //TODO: check field names and cast
    @SuppressWarnings("SwitchStatementWithoutDefaultBranch")
    public ArrayList<PostFull> listPosts(String forum,
                                         String since, Integer limit, String order, ArrayList<String> related)
            throws DbException {
        String postfix = " WHERE Post.forum = '" + forum + '\'';
        if (since != null) {
            postfix += " AND Post.date >= '" + since + "\' ";
        }
        postfix += " GROUP BY ";
        if (related.contains("user"))
            postfix += "User.id,";
        if (related.contains("thread"))
            postfix += "Thread.id,";
        if (related.contains("forum"))
            postfix += "Forum.id,";
        postfix += "Post.id ";
        if (order == null)
            postfix += " ORDER BY Post.date desc";
        else
            postfix += "ORDER BY Post.date " + order;
        if (limit != null)
            postfix += " LIMIT " + limit.toString();
        postfix += ";";

        final StringBuilder tables = new StringBuilder("SELECT Post.*");
        final StringBuilder joins = new StringBuilder(" FROM Post ");

        for (String table : related) {
            switch (table) {
                case "forum":
                    tables.append(" , Forum.*");
                    joins.append(" JOIN Forum ON(Post.forum = Forum.short_name) ");
                    break;
                case "thread":
                    tables.append(" , Thread.*, COUNT(DISTINCT Tpost.id) AS posts");
                    joins.append(" JOIN Thread ON(Post.thread = Thread.id) " +
                            "LEFT JOIN Post AS Tpost ON(Thread.id=Tpost.thread AND Tpost.isDeleted=0) ");
                    break;
                case "user":
                    tables.append(" , User.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                            "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                            "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions ");
                    joins.append(" JOIN User ON(Post.user = User.email) " +
                            "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                            "LEFT JOIN Follow AS Following ON (User.email = Following.follower)  " +
                            "LEFT JOIN Subscription AS Subs ON (User.email = Subs.user) ");
                    break;
            }
        }

        final String query = tables.toString() + joins + postfix;
        //System.out.println(query);
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
                                    resultSet.getBoolean("Post.isDeleted"),
                                    resultSet.getLong("likes"),
                                    resultSet.getLong("dislikes")
                            );

                            if (related.contains("user")) {
                                post.setUser(new UserFull(
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
                                post.setUser(resultSet.getString("Post.user"));
                            }
                            if (related.contains("forum")) {
                                post.setForum(new ForumDataSet(
                                        resultSet.getLong("Forum.id"),
                                        resultSet.getString("Forum.name"),
                                        resultSet.getString("Forum.short_name"),
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
                                        resultSet.getBoolean("Thread.isDeleted"),
                                        resultSet.getLong("Thread.likes"),
                                        resultSet.getLong("Thread.dislikes"),
                                        resultSet.getLong("posts")
                                ));
                            } else {
                                post.setThread(resultSet.getLong("Post.thread"));
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
            postfix += " AND Thread.date >= '" + since + "\' ";
        }
        postfix += " GROUP BY ";
        postfix += "Thread.id ";
        if (related.contains("user"))
            postfix += " ,User.id ";
        if (related.contains("forum"))
            postfix += " ,Forum.id ";
        if (order == null)
            postfix += " ORDER BY Thread.date desc";
        else
            postfix += "ORDER BY Thread.date " + order;
        if (limit != null)
            postfix += " LIMIT " + limit.toString();
        postfix += ";";


        final StringBuilder tables = new StringBuilder("SELECT Thread.*, COUNT(DISTINCT Tpost.id) AS posts ");
        final StringBuilder joins = new StringBuilder(" FROM Thread LEFT JOIN Post AS Tpost ON(Thread.id=Tpost.thread AND Tpost.isDeleted=0)");

        for (String table : related) {
            if (table.equals("forum")) {
                tables.append(" , Forum.*");
                joins.append(" JOIN Forum ON(Thread.forum = Forum.short_name)");
            } else if (table.equals("user")) {
                tables.append(" , User.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                        "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                        "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions ");
                joins.append(" JOIN User ON(Thread.user = User.email) " +
                        "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                        "LEFT JOIN Follow AS Following ON (User.email = Following.follower)  " +
                        "LEFT JOIN Subscription AS Subs ON (User.email = Subs.user) ");
            }
        }

        final String query = tables.toString() + joins + postfix;
        //System.out.println(query);
        try {
            return executor.execQuery(getConnection(), query,
                    resultSet -> {
                        final ArrayList<ThreadFull> result = new ArrayList<>();
                        while (resultSet.next()) {
                            final ThreadFull thread = new ThreadFull(
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

                            if (related.contains("user")) {
                                thread.setUser(new UserFull(
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
                                thread.setUser(resultSet.getString("Thread.user"));
                            }
                            if (related.contains("forum")) {
                                thread.setForum(new ForumDataSet(
                                        resultSet.getLong("Forum.id"),
                                        resultSet.getString("Forum.name"),
                                        resultSet.getString("Forum.short_name"),
                                        resultSet.getString("Forum.user")
                                ));
                            } else {
                                thread.setForum(resultSet.getString("Thread.forum"));
                            }

                            result.add(thread);
                        }

                        return result;
                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get threads or related data!", e);
        }
    }

    public ArrayList<UserFull> listUsers(String forum, Integer since, Integer limit, String order) throws DbException {
        String postfix = " WHERE User.email in (SELECT user FROM Post WHERE Post.forum = '" + forum + "\')";
        if (since != null)
            postfix += " AND User.id >= " + since;
        final String nullOrder;
        if ((order == null) || (order.equals("desc")))
            nullOrder = "asc";
        else
            nullOrder = "desc";
        postfix += " GROUP BY User.id ";
        postfix += " ORDER BY User.isAnonymous " + nullOrder + ", User.name "
                + ((order == null) ? "desc" : order) + ' ';
        if (limit != null)
            postfix += " LIMIT " + limit.toString();
        postfix += ";";


        final String query = "SELECT User.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions " +
                "FROM User " +
                "LEFT JOIN Follow AS Followers ON (User.email=Followers.followee) " +
                "LEFT JOIN Follow AS Following ON (User.email = Following.follower)  " +
                "LEFT JOIN Subscription AS Subs ON (User.email = Subs.user) " + postfix;
        //System.out.println(query);
        try {
            return executor.execQuery(getConnection(), query,
                    resultSet -> {
                        final ArrayList<UserFull> result = new ArrayList<>();
                        while (resultSet.next()) {
                            result.add(new UserFull(
                                    resultSet.getLong("User.id"),
                                    resultSet.getString("User.email"),
                                    resultSet.getString("User.username"),
                                    resultSet.getString("User.about"),
                                    resultSet.getString("User.name"),
                                    resultSet.getBoolean("User.isAnonymous"),
                                    resultSet.getString("followers"),
                                    resultSet.getString("followees"),
                                    resultSet.getString("subscriptions")));
                        }

                        return result;
                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get threads or related data!", e);
        }
    }

}

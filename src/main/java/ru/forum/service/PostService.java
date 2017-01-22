package ru.forum.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.dataset.ForumDataSet;
import ru.forum.model.dataset.PostDataSet;
import ru.forum.model.dataset.ThreadDataSet;
import ru.forum.model.full.PostFull;
import ru.forum.model.full.UserFull;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static ru.forum.helper.Base65.makePath;

@SuppressWarnings({"Duplicates", "unused", "SwitchStatementWithoutDefaultBranch", "OverlyComplexMethod"})
@Service
public class PostService extends AbstractDbService {

    @Autowired
    public PostService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
        try {
            this.dbConnection = DataSourceUtils.getConnection(this.dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DbException("Unable to get database connection!", e);
        }
    }


    public PostDataSet createPost(String date, long thread, String message, String user, String forum, Long parent,
                                  Boolean isApproved, Boolean isHighlighted, Boolean isEdited,
                                  Boolean isSpam, Boolean isDeleted) throws DbException {
        stringBuilder.setLength(0);
        if (parent != null)
            formatter.format("INSERT IGNORE INTO Post(thread,forum,user,message,date,parent,isApproved,isHighlighted,isEdited," +
                        "isSpam,isDeleted) VALUES(%d,'%s','%s','%s','%s',%d,%d,%d,%d,%d,%d);",
                thread, forum, user, message, date, parent, isApproved ? 1 : 0, isHighlighted ? 1 : 0, isEdited ? 1 : 0,
                isSpam ? 1 : 0, isDeleted ? 1 : 0);
        else
            formatter.format("INSERT IGNORE INTO Post(thread,forum,user,message,date,isApproved,isHighlighted,isEdited," +
                            "isSpam,isDeleted) VALUES(%d,'%s','%s','%s','%s',%d,%d,%d,%d,%d);",
                    thread, forum, user, message, date, isApproved ? 1 : 0, isHighlighted ? 1 : 0, isEdited ? 1 : 0,
                    isSpam ? 1 : 0, isDeleted ? 1 : 0);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return null;
        } catch (SQLException e) {
            throw new DbException("Unable to create post!", e);
        }

        stringBuilder.setLength(0);
        formatter.format("SELECT * FROM Post WHERE user='%s' AND thread=%d AND date='%s';", user, thread, date);
        final PostDataSet post;
        try {
            post = executor.execQuery(getConnection(), formatter.toString(), resultSet -> {
                resultSet.next();
                return new PostDataSet(
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
                        resultSet.getLong("dislikes"));
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get post after create!", e);
        }

        if (parent == null) {
            post.setRootParent(post.getId());
            post.setPath(makePath(post.getId()));
        } else {
            final String query = "SELECT root_parent,path FROM Post WHERE id=" + post.getParent().toString() + ';';
            try {
                executor.execQuery(getConnection(), query, resultSet -> {
                    resultSet.next();
                    post.setRootParent(resultSet.getLong("root_parent"));
                    if (resultSet.getString("root_parent") != null)
                        post.setPath(resultSet.getString("path") + makePath(post.getId()));
                    else
                        post.setPath(makePath(post.getId()));
                    return null;
                });
            } catch (SQLException e) {
                throw new DbException("Unable to get post parent!",e);
            }
         }

         try {
             final String update = "UPDATE IGNORE Post SET root_parent=" + post.getRootParent() + ", path='" +
                     post.getPath() + "' WHERE id=" + Long.toString(post.getId()) + ';';
             if (executor.execUpdate(getConnection(), update) == 0)
                 throw new DbException("Unable to update post, query:" + update, null);
         } catch (SQLException e) {
             throw new DbException("Unable to update post!", e);
         }

         return post;
    }

    public PostFull postDetails(long postId, List<String> related) throws DbException {

        String postfix = " WHERE Post.id = " + Long.toString(postId);
        if (related.contains("user"))
            postfix += " GROUP BY User.id";
        if (related.contains("user") && related.contains("forum"))
            postfix += ",Forum.id";
        postfix += ';';

        final StringBuilder tables = new StringBuilder("SELECT Post.*");
        final StringBuilder joins = new StringBuilder(" FROM Post");

        for (String table : related) {
            switch (table) {
                case "forum":
                    tables.append(" , Forum.*");
                    joins.append(" JOIN Forum ON(Post.forum = Forum.short_name)");
                    break;
                case "thread":
                    tables.append(" , Thread.*, COUNT(DISTINCT Tpost.id) AS posts");
                    joins.append(" JOIN Thread ON(Post.thread = Thread.id) " +
                            "  LEFT JOIN Post AS Tpost ON(Thread.id=Tpost.thread AND Tpost.isDeleted=0) ");
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
                        if (!resultSet.next())
                            return null;
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

                        return post;

                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get post or related data!", e);
        }
    }

    public ArrayList<PostFull> listPostsByForum(String forum,
                                                String since, Integer limit, String order)
            throws DbException {
        String query = "SELECT * FROM Post WHERE forum = '" + forum + '\'';
        if (since != null) {
            query += " AND date >= '" + since + '\'';
        }
        if (order == null)
            query += " ORDER BY date desc";
        else
            query += " ORDER BY date " + order;
        if (limit != null)
            query += " LIMIT " + limit.toString();
        query += ";";
        //System.out.println(query);
        try {
            return executor.execQuery(getConnection(), query,
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

    public ArrayList<PostFull> listPostsByThread(int thread,
                                                 String since, Integer limit, String order)
            throws DbException {
        String query = "SELECT * FROM Post WHERE thread = '" + Integer.toString(thread) + '\'';
        if (since != null) {
            query += " AND date >= '" + since + '\'';
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

    public boolean removePost(long postId) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Post SET isDeleted=1 WHERE id = %d;", postId);
        //System.out.println(formatter.toString());
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to remove post!", e);
        }
    }

    public boolean restorePost(long postId) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Post SET isDeleted=0 WHERE id = %d;", postId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to restore post!", e);
        }
    }

    public PostDataSet updatePost(long postId, String message) throws DbException {
        stringBuilder.setLength(0);
        formatter.format("UPDATE IGNORE Post SET message='%s' WHERE id=%d;", message, postId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return null;
        } catch (SQLException e) {
            throw new DbException("Unable to update post!", e);
        }

        stringBuilder.setLength(0);
        formatter.format("SELECT * FROM Post WHERE id = %d", postId);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> {
                resultSet.next();
                return new PostDataSet(
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
                        resultSet.getLong("dislikes"));
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get post after update!", e);
        }
    }

    public PostDataSet votePost(long postId, int vote) throws DbException {
        stringBuilder.setLength(0);
        if (vote == 1) {
            formatter.format("UPDATE IGNORE Post SET likes = likes + 1 WHERE id = %d;", postId);
        } else if (vote == -1) {
            formatter.format("UPDATE IGNORE Post SET dislikes = dislikes + 1 WHERE id = %d;", postId);
        } else
            return null;
        //System.out.println(formatter.toString());
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("Unable to update vote for post!", e);
        }

        stringBuilder.setLength(0);
        formatter.format("SELECT * FROM Post WHERE id = %d", postId);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> {
                resultSet.next();
                return new PostDataSet(
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
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get post after vote!", e);
        }
    }
}

package ru.forum.service;


import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.dataset.ForumDataSet;
import ru.forum.model.dataset.PostDataSet;
import ru.forum.model.dataset.ThreadDataSet;
import ru.forum.model.full.PostFull;
import ru.forum.model.full.UserFull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"Duplicates", "unused", "SwitchStatementWithoutDefaultBranch"})
@Service
public class PostService extends AbstractDbService {

    public PostService() throws DbException {
    }

    public PostDataSet createPost(String date, int thread, String message, String user, String forum, int parent,
                                  boolean isApproved, boolean isHighlighted, boolean isEdited,
                                  boolean isSpam, boolean isDeleted) throws DbException {
        formatter.format("INSERT INTO Post(thread,forum,user,message,date,parent,isApproved,isHighlighted,isEdited,+" +
                        "isSpam,isDeleted) VALUES(%d,'%s','%s','%s','%s',%d,%d,%d.%d.%d.%d);",
                thread, forum, user, message, date, parent, isApproved ? 1 : 0, isHighlighted ? 1 : 0, isEdited ? 1 : 0,
                isSpam ? 1 : 0, isDeleted ? 1 : 0);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return null;
        } catch (SQLException e) {
            throw new DbException("Unable to create post!", e);
        }

        formatter.format("SELECT * FROM Post WHERE user='%s' AND thread=%d AND date='%s';", user, thread, date);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> new PostDataSet(
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
                    resultSet.getLong("likes")
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get post after create!", e);
        }
    }

    public PostFull postDetails(int postId, List<String> related) throws DbException {

        String postfix = " WHERE Post.id = " + Integer.toString(postId);
        if (related.contains("user"))
            postfix += " GROUP BY User.id";
        postfix += ';';

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
                    tables.append(" , Users.*, GROUP_CONCAT(DISTINCT Followers.follower) AS followers, " +
                            "GROUP_CONCAT(DISTINCT Following.followee) AS followees, " +
                            "GROUP_CONCAT(DISTINCT Subs.thread) AS subscriptions ");
                    joins.append(" JOIN User ON(Post.user = User.email) " +
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
                                resultSet.getLong("likes")

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
                                    resultSet.getLong("likes")

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
                                                 String since, Integer limit, String order, ArrayList<String> related)
            throws DbException {
        String query = "SELECT * FROM Post WHERE thread = '" + Integer.toString(thread) + '\'';
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
                                    resultSet.getLong("likes")

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
        formatter.format("UPDATE Post (isDeleted) SET (1) WHERE id = %d;", postId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to remove thread!", e);
        }
    }

    public boolean restorePost(long postId) throws DbException {
        formatter.format("UPDATE Post (isDeleted) SET (0) WHERE id = %d;", postId);
        try {
            return executor.execUpdate(getConnection(), formatter.toString()) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to restore thread!", e);
        }
    }

    public PostDataSet updatePost(long postId, String message) throws DbException {
        formatter.format("UPDATE Post(message) SET('%s') WHERE id=%d;", message, postId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0)
                return null;
        } catch (SQLException e) {
            throw new DbException("Unable to update post!", e);
        }

        formatter.format("SELECT * FROM Post WHERE id = %d", postId);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> new PostDataSet(
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
                    resultSet.getLong("likes")
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get post after update!", e);
        }
    }

    public PostDataSet votePost(long postId, short vote) throws DbException {
        if (vote == 1) {
            formatter.format("UPDATE Thread SET likes = likes + 1 WHERE id = %d;", postId);
        } else if (vote == -1) {
            formatter.format("UPDATE Thread SET likes = likes - 1 WHERE id = %d;", postId);
        } else
            return null;
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("Unable to update vote for post!", e);
        }

        formatter.format("SELECT * FROM Post WHERE id = %d", postId);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> new PostDataSet(
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
                    resultSet.getLong("likes")
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get post after vote!", e);
        }
    }
}

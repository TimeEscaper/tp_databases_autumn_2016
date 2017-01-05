package ru.forum.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.DataSet.ForumDataSet;
import ru.forum.model.DataSet.PostDataSet;
import ru.forum.model.DataSet.ThreadDataSet;
import ru.forum.model.Full.PostFull;
import ru.forum.model.Full.UserFull;
import sun.util.locale.StringTokenIterator;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
@Service
public class PostService extends AbstractDbService {

    public PostService() throws DbException { }

    public PostDataSet createPost(String date, int thread, String message, String user, String forum, int parent,
                                  boolean isApproved, boolean isHighlighted, boolean isEdited,
                                  boolean isSpam, boolean isDeleted) throws DbException
    {
        formatter.format("INSERT INTO Post(thread,forum,user,message,date,parent,isApproved,isHighlighted,isEdited,+" +
                "isSpam,isDeleted) VALUES(%d,'%s','%s','%s','%s',%d,%d,%d.%d.%d.%d);",
                thread, forum, user, message, date, parent, isApproved ? 1:0, isHighlighted ? 1:0, isEdited ? 1:0,
                isSpam ? 1:0, isDeleted ? 1:0);
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
                    resultSet.getBoolean("isDeleted")
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get post after create!", e);
        }
    }

    public PostFull postDetails(int postId, List<String> related) throws DbException {

        final String postfix = " WHERE Post.id = " + Integer.toString(postId) + ';';

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

                            return post;

                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get posts or related data!", e);
        }
    }


}

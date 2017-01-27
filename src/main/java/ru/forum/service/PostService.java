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
import ru.forum.model.dataset.ThreadDataSet;
import ru.forum.model.full.PostFull;
import ru.forum.model.full.UserFull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static ru.forum.helper.Base65.makePath;
import static ru.forum.helper.QueryHelper.format;

@SuppressWarnings({"Duplicates", "unused", "SwitchStatementWithoutDefaultBranch", "OverlyComplexMethod"})
@Component
public class PostService extends AbstractDbService {

    private GetService getService;

    @Autowired
    public PostService(DataSource dataSource, GetService getService) throws DbException {
        this.dataSource = dataSource;
        this.getService = getService;
    }


    public PostDataSet createPost(String date, long thread, String message, String user, String forum, Long parent,
                                  Boolean isApproved, Boolean isHighlighted, Boolean isEdited,
                                  Boolean isSpam, Boolean isDeleted) throws DbException {
        String query;
        if (parent != null)
            query = format("INSERT IGNORE INTO Post(thread,forum,user,message,date,parent,isApproved,isHighlighted,isEdited," +
                        "isSpam,isDeleted) VALUES(%d,'%s','%s','%s','%s',%d,%d,%d,%d,%d,%d);",
                thread, forum, user, message, date, parent, isApproved ? 1 : 0, isHighlighted ? 1 : 0, isEdited ? 1 : 0,
                isSpam ? 1 : 0, isDeleted ? 1 : 0);
        else
            query = format("INSERT IGNORE INTO Post(thread,forum,user,message,date,isApproved,isHighlighted,isEdited," +
                            "isSpam,isDeleted) VALUES(%d,'%s','%s','%s','%s',%d,%d,%d,%d,%d);",
                    thread, forum, user, message, date, isApproved ? 1 : 0, isHighlighted ? 1 : 0, isEdited ? 1 : 0,
                    isSpam ? 1 : 0, isDeleted ? 1 : 0);
        try (Connection connection = getConnection()) {
            if (executor.execUpdate(connection, query) == 0)
                return null;
        } catch (SQLException e) {
            System.out.println(query);
            throw new DbException("Unable to create post!", e);
        }

        query = format("UPDATE IGNORE Thread SET posts=posts+1 WHERE id=%d;", thread);
        try (Connection connection = getConnection()) {
            if (executor.execUpdate(connection, query) == 0)
                return null;
        } catch (SQLException e) {
            System.out.println(query);
            throw new DbException("Unable to create post!", e);
        }

        query = format("SELECT * FROM Post WHERE user='%s' AND thread=%d AND date='%s';", user, thread, date);
        final PostDataSet post;
        try (Connection connection = getConnection()) {
            post = executor.execQuery(connection, query, resultSet -> {
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
            final String queryParent = "SELECT root_parent,path FROM Post WHERE id=" + post.getParent().toString() + ';';
            try (Connection connection = getConnection()) {
                executor.execQuery(connection, queryParent, resultSet -> {
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

         try (Connection connection = getConnection()) {
             final String update = "UPDATE IGNORE Post SET root_parent=" + post.getRootParent() + ", path='" +
                     post.getPath() + "' WHERE id=" + Long.toString(post.getId()) + ';';
             if (executor.execUpdate(connection, update) == 0)
                 throw new DbException("Unable to update post, query:" + update, null);
         } catch (SQLException e) {
             throw new DbException("Unable to update post!", e);
         }

         return post;
    }

    public PostFull postDetails(long postId, List<String> related) throws DbException {

        final String query = "SELECT Post.* FROM Post WHERE Post.id = " + Long.toString(postId);

        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query,
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
                            post.setUser(getService.getUserFull(resultSet.getString("Post.user")));
                        } else {
                            post.setUser(resultSet.getString("Post.user"));
                        }
                        if (related.contains("forum")) {
                            post.setForum(getService.getForum(resultSet.getString("Post.forum")));
                        } else {
                            post.setForum(resultSet.getString("Post.forum"));
                        }
                        if (related.contains("thread")) {
                            post.setThread(getService.getThread(resultSet.getLong("Post.thread")));
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

    public boolean removePost(long postId) throws DbException {
        String query = format("UPDATE IGNORE Post SET isDeleted=1 WHERE id = %d;", postId);
        //System.out.println(formatter.toString());
        //System.out.println(query);
        try (Connection connection = getConnection()) {
            if (executor.execUpdate(connection, query) == 0)
                return false;
        } catch (SQLException e) {
            throw new DbException("Unable to remove post!", e);
        }

        query = format("UPDATE IGNORE Thread SET posts=posts-1 WHERE id=(SELECT thread FROM Post WHERE id= %d );", postId);
        //System.out.println(query);
        try (Connection connection = getConnection()) {
            return executor.execUpdate(connection, query) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to remove post!", e);
        }
    }

    public boolean restorePost(long postId) throws DbException {
        String query = format("UPDATE IGNORE Post SET isDeleted=0 WHERE id = %d;", postId);
        try (Connection connection = getConnection()) {
            if (executor.execUpdate(connection, query) == 0)
                return false;
        } catch (SQLException e) {
            throw new DbException("Unable to restore post!", e);
        }

        query = format("UPDATE IGNORE Thread SET posts=posts+1 WHERE id=(SELECT thread FROM Post WHERE id= %d );", postId);
        try (Connection connection = getConnection()) {
            return executor.execUpdate(connection, query) != 0;
        } catch (SQLException e) {
            throw new DbException("Unable to restore post!", e);
        }
    }

    public PostDataSet updatePost(long postId, String message) throws DbException {
        String query = format("UPDATE IGNORE Post SET message='%s' WHERE id=%d;", message, postId);
        try (Connection connection = getConnection()) {
            if (executor.execUpdate(connection, query) == 0)
                return null;
        } catch (SQLException e) {
            throw new DbException("Unable to update post!", e);
        }

        query = format("SELECT * FROM Post WHERE id = %d", postId);
        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query, resultSet -> {
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
        String query;
        if (vote == 1) {
            query = format("UPDATE IGNORE Post SET likes = likes + 1 WHERE id = %d;", postId);
        } else if (vote == -1) {
            query = format("UPDATE IGNORE Post SET dislikes = dislikes + 1 WHERE id = %d;", postId);
        } else
            return null;
        //System.out.println(formatter.toString());
        try (Connection connection = getConnection()) {
            if (executor.execUpdate(connection, query) == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new DbException("Unable to update vote for post!", e);
        }

        query = format("SELECT * FROM Post WHERE id = %d", postId);
        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query, resultSet -> {
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

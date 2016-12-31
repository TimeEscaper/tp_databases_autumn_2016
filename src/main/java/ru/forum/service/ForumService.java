package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.database.executor.Executor;
import ru.forum.model.ForumDataSet;
import ru.forum.model.PostFull;
import ru.forum.model.ThreadDataSet;
import ru.forum.model.UserDataSet;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

@Service
public class ForumService extends AbstractDbService {

    @Autowired
    public ForumService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
    }

    public ForumDataSet createForum(String name, String shortName, String user) throws DbException {
        final Connection connection = getConnection();
        final StringBuilder sqlUpdate = new StringBuilder();
        try (Formatter formatter = new Formatter(sqlUpdate, Locale.US)) {
            formatter.format("INSERT INTO Forum(name, short_name, user) VALUES('%s','%s','%s');",
                    name, shortName, user);
            final Executor executor = new Executor();

            try {
                final int updated = executor.execUpdate(connection, formatter.toString());
                if (updated == 0)
                    return null;

                formatter.format("SELECT * FROM Forum WHERE short_name = '%s';", shortName);
                return executor.execQuery(connection, formatter.toString(), resultSet -> new ForumDataSet(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("short_name"),
                        resultSet.getString("user")
                ));

            } catch (SQLException e) {
                throw new DbException("Unable to create forum!", e);
            }
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
    public ArrayList<PostFull> listPosts(String forum,
                                         String since, Integer limit, String order, ArrayList<String> related)
            throws DbException {
        String postfix = "";
        if (since != null) {
            postfix += " WHERE Post.date > " + since;
        }
        if (order == null)
            postfix += " ORDER BY Post.date desc";
        else
            postfix += "ORDER BY Post.date " + order;
        if (limit != null)
            postfix += " LIMIT " + limit.toString();
        postfix += ";";

        String tables = "SELECT Post.*";
        String joins = "FROM Post";
        for (String table : related) {
            if (table.equals("forum")) {
                tables += " , forum.*";
                joins += " JOIN Forum ON(Post.forum = Forum.short_name)";
            }
            else if (table.equals("thread")) {
                tables += " , Thread.*";
                joins += " JOIN Thread ON(Post.thread = Thread.id)";
            }
            else if (table.equals("user")) {
                tables += " , User.*";
                joins += " JOIN User ON(Post.user = User.email)";
            }
        }

        final String query = tables + joins + postfix;

        try {
            executor.execQuery(getConnection(), query,
                    resultSet -> {
                        final List<PostFull> resuslt = new ArrayList<>();
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
                                post.setUser(new UserDataSet(
                                        resultSet.getLong("User.id"),
                                        resultSet.getString("User.email"),
                                        resultSet.getString("User.username"),
                                        resultSet.getString("User.about"),
                                        resultSet.getString("User.name"),
                                        resultSet.getBoolean("User.isAnonymous")
                                ));
                            }
                            else {
                                post.setUser(resultSet.getString("Post.user"));
                            }
                            if (related.contains("forum")) {
                                post.setForum(new ForumDataSet(
                                        resultSet.getLong("Forum.id"),
                                        resultSet.getString("Forum.name"),
                                        resultSet.getString("Forum.shortName"),
                                        resultSet.getString("Forum.user")
                                ));
                            }
                            else {
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
                            }
                            else {
                                post.setThread(resultSet.getString("Post.thread"));
                            }

                            resuslt.add(post);
                        }

                        return resuslt;
                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get posts or related data!", e);
        }

        return null;
    }
}

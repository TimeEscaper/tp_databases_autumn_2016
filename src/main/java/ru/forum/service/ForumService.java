package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
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

import javax.jws.soap.SOAPBinding;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@SuppressWarnings({"unused", "OverlyComplexMethod", "Duplicates"})
@Component
public class ForumService extends AbstractDbService {

    private GetService getService;

    @Autowired
    public ForumService(DataSource dataSource, GetService getService)
            throws DbException {
        this.dataSource = dataSource;
        this.getService = getService;
    }

    public ForumDataSet createForum(String name, String shortName, String user) throws DbException {
        String query = QueryHelper.format("INSERT IGNORE INTO Forum(name, short_name, user) VALUES('%s','%s','%s');",
                name, shortName, user);
        //System.out.println(formatter.toString());
        try(Connection connection = getConnection()) {
            if (executor.execUpdate(connection, query) == 0)
                return null;
        } catch (SQLException e) {
            //System.out.println(formatter.toString());
            throw new DbException("Unable to create forum!", e);
        }

        query = QueryHelper.format("SELECT * FROM Forum WHERE short_name = '%s';", shortName);
        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query, resultSet -> {
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

    public ForumFull forumDetails(String forum, String user) throws DbException {

        String query = "SELECT Forum.* FROM Forum WHERE Forum.short_name = '" + forum + "\';";
        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query,
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
                            result.setUser(getService.getUserFull(resultSet.getString("Forum.user")));
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
        String query = "SELECT Post.* FROM Post WHERE Post.forum = '" + forum + '\'';
        if (since != null) {
            query += " AND Post.date >= '" + since + "\' ";
        }

        if (order == null)
            query += " ORDER BY Post.date desc";
        else
            query += "ORDER BY Post.date " + order;
        if (limit != null)
            query += " LIMIT " + limit.toString();
        query += ";";


        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query,
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

        String query = "SELECT Thread.* FROM Thread WHERE Thread.forum = '" + forum + '\'';
        if (since != null) {
            query += " AND Thread.date >= '" + since + "\' ";
        }
        query += " GROUP BY ";
        query += "Thread.id ";
        if (order == null)
            query += " ORDER BY Thread.date desc";
        else
            query += "ORDER BY Thread.date " + order;
        if (limit != null)
            query += " LIMIT " + limit.toString();
        query += ";";

        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query,
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
                                    resultSet.getLong("Thread.posts")
                            );

                            if (related.contains("user")) {
                                thread.setUser(getService.getUserFull(
                                        resultSet.getString("Thread.user")
                                ));
                            } else {
                                thread.setUser(resultSet.getString("Thread.user"));
                            }
                            if (related.contains("forum")) {
                                thread.setForum(getService.getForum(resultSet.getString("Thread.forum")));
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
        String query = "SELECT User.email FROM User JOIN Post ON (Post.user=User.email) WHERE Post.forum = '" +
                forum + "' ";
        if (since != null)
            query += " AND User.id >= " + since;
        final String nullOrder;
        if ((order == null) || (order.equals("desc")))
            nullOrder = "asc";
        else
            nullOrder = "desc";
        query += " GROUP BY User.id ";
        query += " ORDER BY User.isAnonymous " + nullOrder + ", User.name "
                + ((order == null) ? "desc" : order) + ' ';
        if (limit != null)
            query += " LIMIT " + limit.toString();
        query += ";";

        try (Connection connection = getConnection()) {
            return executor.execQuery(connection, query,
                    resultSet -> {
                        final ArrayList<UserFull> result = new ArrayList<>();
                        while (resultSet.next()) {
                            result.add(getService.getUserFull(resultSet.getString("User.email")));
                        }

                        return result;
                    });
        } catch (SQLException e) {
            throw new DbException("Unable to get threads or related data!", e);
        }
    }

}

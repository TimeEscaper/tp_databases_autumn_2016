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

    public ArrayList<PostFull> listPosts(String forum,
                                         String since, Integer limit, String order, ArrayList<String> related) {

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

        String query = tables + joins + postfix;

        String forumClass = related.contains("forum") ? ForumDataSet.class.getName() : String.class.getName();
        String threadClass = related.contains("thread") ? ThreadDataSet.class.getName() : String.class.getName();
        String userClass = related.contains("user") ? UserDataSet.class.getName() : String.class.getName();

        /*try {
            executor.execQuery(getConnection(), query,
                    resultSet -> {
                        PostFull<forumClass,  >
                    })
        } */

        return null;
    }
}

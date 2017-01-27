package ru.forum.service;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.helper.QueryHelper;
import ru.forum.model.dataset.ForumDataSet;
import ru.forum.model.dataset.ThreadDataSet;
import ru.forum.model.full.UserFull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class GetService extends AbstractDbService {

    public GetService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
    }

    public UserFull getUserFull(String email) throws SQLException {
        String query = "SELECT * FROM User WHERE email='" + email + "';";
        final UserFull user;
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            user = executor.execQuery(connection, query, resultSet -> {
                if (!resultSet.next())
                    return null;
                return new UserFull(resultSet.getLong("id"),
                        resultSet.getString("email"),
                        resultSet.getString("username"),
                        resultSet.getString("about"),
                        resultSet.getString("name"),
                        resultSet.getBoolean("isAnonymous"));
            });
        } catch (SQLException e) {
            throw e;
        }


        query = "SELECT follower FROM Follow WHERE followee='" + email + "';";
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            executor.execQuery(connection, query, resultSet -> {
                while (resultSet.next())
                    user.addFollower(resultSet.getString("follower"));
                return null;
            });
        } catch (SQLException e) {
            throw e;
        }

        query = "SELECT followee FROM Follow WHERE follower='" + email + "';";
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            executor.execQuery(connection, query, resultSet -> {
                while (resultSet.next())
                    user.addFollowee(resultSet.getString("followee"));
                return null;
            });
        } catch (SQLException e) {
            throw e;
        }

        query = "SELECT thread FROM Subscription WHERE user='" + email + "';";
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            executor.execQuery(connection, query, resultSet -> {
                while (resultSet.next())
                    user.addSubscription(resultSet.getLong("thread"));
                return null;
            });
        } catch (SQLException e) {
            throw e;
        }

        return user;
    }

    public ForumDataSet getForum(String shortName) throws SQLException {
        final String query = QueryHelper.format("SELECT * FROM Forum WHERE short_name = '%s';", shortName);
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            return executor.execQuery(connection, query, resultSet -> {
                resultSet.next();
                return new ForumDataSet(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("short_name"),
                        resultSet.getString("user"));
            });
        } catch (SQLException e) {
            throw e;
        }
    }

    public ThreadDataSet getThread(long id) throws SQLException {
        final String query = "SELECT * FROM Thread WHERE id=" + Long.toString(id) + ';';
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            return executor.execQuery(connection, query, resultSet -> {
                if (!resultSet.next())
                    return null;
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
            throw e;
        }
    }
}

package ru.forum.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.DbStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("unused")
@Component
public class CommonDbService  extends AbstractDbService{

    @Autowired
    public CommonDbService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
    }

    public DbStatus getStatus() throws DbException {
        try(Connection connection = getConnection()) {
            String query = "SELECT COUNT(*) AS users FROM User;";
            final long users = executor.execQuery(connection, query, resultSet -> {
                resultSet.next();
                return resultSet.getLong("users");
            });

            query = "SELECT COUNT(*) AS forums FROM Forum;";
            final long forums = executor.execQuery(getConnection(), query, resultSet -> {
                resultSet.next();
                return resultSet.getLong("forums");
            });

            query = "SELECT COUNT(*) AS threads FROM Thread;";
            final long threads = executor.execQuery(getConnection(), query, resultSet -> {
                resultSet.next();
                return resultSet.getLong("threads");
            });

            query = "SELECT COUNT(*) AS posts FROM Post;";
            final long posts = executor.execQuery(getConnection(), query, resultSet -> {
                resultSet.next();
                return resultSet.getLong("posts");
            });

            return new DbStatus(users, threads, forums, posts);

        } catch (SQLException e) {
            throw new DbException("Ubable to get database status!", e);
        }
    }

    public void truncateAll() throws DbException {
        try(Connection connection = getConnection()) {
            executor.execTruncate(connection, "SET FOREIGN_KEY_CHECKS=0;");
            executor.execTruncate(connection, "TRUNCATE TABLE User;");
            executor.execTruncate(connection, "TRUNCATE TABLE Forum");
            executor.execTruncate(connection, "TRUNCATE TABLE Thread");
            executor.execTruncate(connection, "TRUNCATE TABLE Post;");
            executor.execTruncate(connection, "TRUNCATE TABLE Follow;");
            executor.execTruncate(connection, "TRUNCATE TABLE Subscription;");
            executor.execTruncate(connection, "SET FOREIGN_KEY_CHECKS=1;");
        } catch (SQLException e) {
            throw new DbException("Unable to truncate table!", e);
        }
    }
}

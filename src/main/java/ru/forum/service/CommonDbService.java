package ru.forum.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.DbStatus;

import javax.sql.DataSource;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class CommonDbService  extends AbstractDbService{

    @Autowired
    public CommonDbService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
        try {
            this.dbConnection = DataSourceUtils.getConnection(this.dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DbException("Unable to get database connection!", e);
        }
    }

    public DbStatus getStatus() throws DbException {
        try {
            final String query = "SELECT COUNT(User.*) AS users, COUNT(Thread.*) AS threads, COUNT(Forum.*) AS forums, " +
                    "COUNT(Post.*) AS posts FROM User,Thread,Forum,Post;";
            return executor.execQuery(getConnection(), query, resultSet -> new DbStatus(
                    resultSet.getLong("users"),
                    resultSet.getLong("threads"),
                    resultSet.getLong("forums"),
                    resultSet.getLong("posts")
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get database status!", e);
        }
    }

    public void truncateAll() throws DbException {
        try {
            executor.execUpdate(getConnection(), "TRUNCATE TABLE User;");
            executor.execUpdate(getConnection(), "TRUNCATE TABLE Forum");
            executor.execUpdate(getConnection(), "TRUNCATE TABLE Thread");
            executor.execUpdate(getConnection(), "TRUNCATE TABLE Post;");
        } catch (SQLException e) {
            throw new DbException("Unable to truncate table!", e);
        }
    }
}

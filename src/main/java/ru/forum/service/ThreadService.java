package ru.forum.service;

import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.DataSet.ThreadDataSet;

import java.sql.SQLException;

@Service
public class ThreadService extends AbstractDbService {

    public ThreadService() throws DbException { }

    public ThreadDataSet createThread(String forum, String title, String user, String date, String message,
                                      String slug, boolean isClosed, boolean isDeleted) throws DbException {

        formatter.format("INSERT INTO Thread(forum,title,user,date,message,slug,isClosed,isDeleted) " +
                "VALUES('%s','%s','%s','%s','%s','%s','%d','%d');", forum, title, user, date, message, slug,
                isClosed ? 1 : 0, isDeleted ? 1 : 0);
        try {
            final int updated = executor.execUpdate(getConnection(), formatter.toString());
            if (updated == 0) {
                return null;
            }
        }  catch (SQLException e) {
            throw new DbException("Unable to create thread!", e);
        }

        formatter.format("SELECT * FROM Thread WHERE forum = '%s' AND title = '%s' AND date = '%s';",
                forum, title, date);
        try {
            return executor.execQuery(getConnection(), formatter.toString(), resultSet -> new ThreadDataSet(
                    resultSet.getLong("id"),
                    resultSet.getString("forum"),
                    resultSet.getString("user"),
                    resultSet.getString("date"),
                    resultSet.getString("title"),
                    resultSet.getString("slug"),
                    resultSet.getString("message"),
                    resultSet.getBoolean("isClosed"),
                    resultSet.getBoolean("isDeleted")
            ));
        } catch (SQLException e) {
            throw new DbException("Unable to get thread!", e);
        }
    }

}

package ru.forum.service;

import org.springframework.scheduling.config.SchedulerBeanDefinitionParser;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.DataSet.SubscriptionDataSet;
import ru.forum.model.DataSet.ThreadDataSet;

import java.sql.SQLException;
import java.util.ArrayList;

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

    public boolean closeThread(long threadId) throws DbException {
        formatter.format("UPDATE Thread (isClosed) SET (1) WHERE id = %d;", threadId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return false;
            };
            return true;
        } catch (SQLException e) {
            throw new DbException("Unable to close thread!", e);
        }
    }

    //TODO: filter creator in utils
    public ArrayList<ThreadDataSet> listThread(String filter, boolean isByUser,
                                                     String since, Integer limit, String order) throws DbException {

        String postfix;
        if (isByUser)
            postfix = " WHERE Thread.user = '" + filter + "'";
        else
            postfix = " WHERE Thread.forum = '" + filter + "'";

        if (since != null) {
            postfix += " AND Thread.date >= " + since;
        }
        if (order == null)
            postfix += " ORDER BY Thread.date desc";
        else
            postfix += "ORDER BY Post.date " + order;
        if (limit != null)
            postfix += " LIMIT " + limit.toString();
        postfix += ";";

        String query = "SELECT * FROM Thread" + postfix;

        try {
            return executor.execQuery(getConnection(), query, resultSet -> {
                ArrayList<ThreadDataSet> result = new ArrayList<>();
                while (resultSet.next()) {
                    ThreadDataSet thread = new ThreadDataSet(
                            resultSet.getLong("id"),
                            resultSet.getString("forum"),
                            resultSet.getString("user"),
                            resultSet.getString("date"),
                            resultSet.getString("title"),
                            resultSet.getString("slug"),
                            resultSet.getString("message"),
                            resultSet.getBoolean("isClosed"),
                            resultSet.getBoolean("isDeleted")
                    );
                    result.add(thread);
                }

                return result;
            });
        } catch (SQLException e) {
            throw new DbException("Unable to get threads by user!", e);
        }
    }

    public boolean openThread(long threadId) throws DbException {
        formatter.format("UPDATE Thread (isClosed) SET (0) WHERE id = %d;", threadId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return false;
            };
            return true;
        } catch (SQLException e) {
            throw new DbException("Unable to open thread!", e);
        }
    }

    public boolean removeThread(long threadId) throws DbException {
        formatter.format("UPDATE Thread (isDeleted) SET (1) WHERE id = %d;", threadId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return false;
            };
            return true;
        } catch (SQLException e) {
            throw new DbException("Unable to remove thread!", e);
        }
    }

    public boolean restoreThread(long threadId) throws DbException {
        formatter.format("UPDATE Thread (isDeleted) SET (0) WHERE id = %d;", threadId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return false;
            };
            return true;
        } catch (SQLException e) {
            throw new DbException("Unable to restore thread!", e);
        }
    }

    //TODO: check if already subscribe (through DB schema or through query)
    public SubscriptionDataSet subscribeThread(String userId, long threadId) throws DbException {
        formatter.format("INSERT INTO Subscription(thread, user) VALUES(%d, '%s');", threadId, userId);
        try {
            if (executor.execUpdate(getConnection(), formatter.toString()) == 0) {
                return null;
            }
            return new SubscriptionDataSet(threadId, userId);
        } catch (SQLException e) {
            throw new DbException("Unable to subscribe user!", e);
        }
    }

    /*public boolean unsubscribeThread(String userId, long threadId) {
        formatter.format("DELETE From ")
    }*/

}

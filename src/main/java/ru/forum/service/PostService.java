package ru.forum.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;
import ru.forum.model.DataSet.PostDataSet;

import javax.sql.DataSource;
import java.sql.SQLException;

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
    
}

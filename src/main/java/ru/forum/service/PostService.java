package ru.forum.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;
import ru.forum.database.exception.DbException;

import javax.sql.DataSource;

@Service
public class PostService extends AbstractDbService {

    @Autowired
    public PostService(DataSource dataSource) throws DbException {
        this.dataSource = dataSource;
    }
}

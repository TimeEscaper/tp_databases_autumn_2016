package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;

import javax.sql.DataSource;

@Service
public class ForumService extends AbstractDbService {

    @Autowired
    public ForumService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}

package ru.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.forum.database.AbstractDbService;

import javax.sql.DataSource;

@Service
public class ThreadService extends AbstractDbService {

    @Autowired
    public ThreadService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}

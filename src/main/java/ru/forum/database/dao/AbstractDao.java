package ru.forum.database.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public abstract class AbstractDao {

    private Connection connection;

    public AbstractDao(Connection connection) {
        this.connection = connection;
    }
}

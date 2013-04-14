package com.gelicon.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Фабрика для создания соединений с БД.
 */
public interface ConnectionFactory {

    /**
     * Создает соединение с БД.
     * @throws SQLException
     */
    public Connection createConnection() throws SQLException;

}
package com.gelicon.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Реализация соединения в пуле.
 */
public class PoolableConnection extends ConnectionWrapper {

    final ConnectionPool pool;

    /**
     * Конструктор.
     * @param pool пул
     * @throws SQLException
     */
    public PoolableConnection(ConnectionPool pool) throws SQLException {
        super(pool.lockConnection());
        this.pool = pool;
    }

    /**
     * Получение реального соединения.
     */
    public Connection getDelegateConnection() {
        return connection;
    }

    /**
     * Закрытие реального соединения.
     * @throws SQLException
     */
    public void reallyClose() throws SQLException {
        connection.close();
        connection = null;
    }

    /**
     * Закрытие соединения.
     */
    public void close() throws SQLException {
        if (!connection.getAutoCommit()) connection.rollback();
        connection.setAutoCommit(true);
        pool.unlockConnection(connection);
        connection = null;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return connection == null || connection.isClosed();
    }

    protected void finalize() throws Throwable {
        close();
    }

}
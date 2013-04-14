package com.gelicon.dbcp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

/**
 * Реализация пула соединений с БД.
 */
public class ConnectionPool {

    private final ConnectionFactory connectionFactory;

    private final int minPoolSize;

    private final int maxPoolSize;

    private final Vector<Connection> buf = new Vector<Connection>();

    /**
     * Конструктор.
     * @param connectionFactory фабрика для создания новых соединений
     * @param minPoolSize минимальное количество соединений в пуле
     * @param maxPoolSize максимальное количество соединений в пуле
     */
    public ConnectionPool(ConnectionFactory connectionFactory, int minPoolSize,
            int maxPoolSize) {
        this.connectionFactory = connectionFactory;
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * Добавляет новое соединение в пул.
     * @param c соединение с БД
     */
    public void addConnection(Connection c) {
        buf.add(c);
    }

    /**
     * Возвращает соединение из пула. <br>
     * При необходимости создает новое с помощью фабрики.
     * @throws SQLException
     */
    public PoolableConnection getConnection() throws SQLException {
        return new PoolableConnection(this);
    }

    /**
     * Возвращает текущее количество соединений в пуле.
     */
    public int getPoolSize() {
        return buf.size();
    }

    synchronized Connection lockConnection() throws SQLException {

        // from pool
        while (!buf.isEmpty()) {
            Connection tmp = (Connection) buf.remove(0);
            if (checkConnection(tmp)) return tmp;
        }

        // new connection
        try {
            return connectionFactory.createConnection();
        } finally {
            startThread();
        }

    }

    synchronized void unlockConnection(Connection c) throws SQLException {
        if (buf.size() < maxPoolSize)
            buf.add(c);
        else
            closeConnection(c);
    }

    private static boolean checkConnection(Connection c) {
        try {
            if (c.getMetaData().getURL().contains("firebird")) {
                // проверка коннекта для firebird
                // выполним запрос
                c.setAutoCommit(true);
                Statement sql = c.createStatement();
                try {
                    ResultSet rs =
                            sql.executeQuery("SELECT * FROM RDB$DATABASE");
                    try {
                        return rs.next();
                    } finally {
                        rs.close();
                    }
                } finally {
                    sql.close();
                }
            } else {
                c.setAutoCommit(false);
                c.commit();
                c.setAutoCommit(true);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void closeConnection(Connection c) {
        try {
            c.close();
        } catch (Exception ex) {
            // silent
        }
    }

    private void startThread() {
        new ConnectorThread().start();
    }

    /**
     * Закрывает все соединения в пуле.
     */
    public void close() {
        synchronized (this) {
            while (!buf.isEmpty())
                closeConnection((Connection) buf.remove(0));
        }
    }

    protected void finalize() throws Throwable {
        close();
    }

    private class ConnectorThread extends Thread {

        public void run() {
            try {
                while (buf.size() < minPoolSize)
                    addConnection(connectionFactory.createConnection());
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }

    }

    public String displayInfo() {
        return "ConnectionFactory: "
                + connectionFactory
                + "\r\n"
                + "PoolSize: "
                + Integer.toString(buf.size())
                + "\r\n"
                + "MinPoolSize: "
                + Integer.toString(minPoolSize)
                + "\r\n"
                + "MaxPoolSize: "
                + Integer.toString(maxPoolSize)
                + "\r\n";
    }

    public String toString() {
        return displayInfo();
    }

}
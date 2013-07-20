package com.gelicon.dbcp;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Менеджер соединений с БД.
 * @author lan
 */
public class ConnectionManager {

    private final Hashtable<String, ConnectionPool> map =
            new Hashtable<String, ConnectionPool>();

    /**
     * Регистрация пула.<br>
     * Если пул с таким псевдонимом присутствует, то он заменяется.
     * @param alias псевдоним
     * @param pool пул соединений с БД
     */
    public void registerPool(String alias, ConnectionPool pool) {
        synchronized (this) {
            unregisterPool(alias);
            map.put(alias, pool);
        }
    }

    /**
     * Удаление пула и списка.
     * @param alias псевдоним
     */
    public void unregisterPool(String alias) {
        synchronized (this) {
            ConnectionPool tmp = (ConnectionPool) map.remove(alias);
            if (tmp != null) tmp.close();
        }
    }

    /**
     * Возвращает соединение с БД.
     * @param alias псевдоним
     * @return gelicon.dbcp.PoolableConnection
     * @throws SQLException
     */
    public PoolableConnection getConnection(String alias) throws SQLException {
        ConnectionPool pool = (ConnectionPool) map.get(alias);
        if (pool == null)
            throw new SQLException("Псевдоним БД \"" + alias + "\" не найден");
        return ((ConnectionPool) map.get(alias)).getConnection();

    }

    /**
     * Закрытие всех соединений во всех пулах.
     */
    public void close() {
        close(false);
    }

    /**
     * Закрытие всех соединений во всех пулах и очистка списка пулов.
     */
    public void clear() {
        close(true);
    }

    private void close(boolean clear) {
        synchronized (this) {
            Enumeration<ConnectionPool> e = map.elements();
            while (e.hasMoreElements())
                (e.nextElement()).close();
            if (clear) map.clear();
        }
    }

}

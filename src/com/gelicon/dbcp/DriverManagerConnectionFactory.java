package com.gelicon.dbcp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Реализация фабрики соединений с БД. <br>
 * Используется java.sql.DriverManager.
 */
public class DriverManagerConnectionFactory implements ConnectionFactory {

    private final String driver;

    private final String url;

    private final Properties properties;

    /**
     * Конструктор.
     * @param driver имя класса драйвера
     * @param url путь до БД
     * @param properties свойства соединения
     * @throws ClassNotFoundException
     */
    public DriverManagerConnectionFactory(String driver, String url,
            Properties properties) throws ClassNotFoundException {
        this.driver = driver;
        this.url = url;
        this.properties = properties;
        Class.forName(this.driver);
    }

    /**
     * @see gelicon.dbcp.ConnectionFactory#createConnection()
     */
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, properties);
    }

    public String toString() {
        return "DriverManagerConnectionFactory (" + this.getClass().getName()
                + ")\r\n" + "Driver: " + driver + "\r\n" + "Url: " + url
                + "\r\n" + "Properties: " + properties + "\r\n";
    }

}
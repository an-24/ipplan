package com.cantor.ipplan.server;

import com.gelicon.dbcp.ConnectionPool;
import com.gelicon.dbcp.DriverManagerConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

public class IPPlanPoolConnection implements ConnectionProvider {
	
	static private ConnectionPool pool;

	public IPPlanPoolConnection() throws ClassNotFoundException {
		setPool("jdbc:firebirdsql:localhost:D:\\Database\\IPPLAN_UP.FDB");
	}

	static public void setPool(String dbUrl) throws ClassNotFoundException {
		Properties prop = new Properties();
		prop.setProperty("user", "SYSDBA");
		prop.setProperty("password", "masterkey");
		pool = new ConnectionPool(new DriverManagerConnectionFactory("org.firebirdsql.jdbc.FBDriver",dbUrl,prop), 1, 4);
	}

	static public void setPool(ConnectionPool p) {
		pool = p;
	}

	@Override
	public boolean isUnwrappableAs(Class arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeConnection(Connection c) throws SQLException {
		c.close();

	}

	@Override
	public Connection getConnection() throws SQLException {
		return pool.getConnection();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		// TODO Auto-generated method stub
		return false;
	}
	

}

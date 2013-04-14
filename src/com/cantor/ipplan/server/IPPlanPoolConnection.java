package com.cantor.ipplan.server;

import com.gelicon.dbcp.ConnectionFactory;
import com.gelicon.dbcp.ConnectionPool;
import com.gelicon.dbcp.DriverManagerConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

public class IPPlanPoolConnection implements ConnectionProvider {
	
	private ConnectionPool pool;

	public IPPlanPoolConnection() throws ClassNotFoundException {
		
		Properties prop = new Properties();
		prop.setProperty("user", "SYSDBA");
		prop.setProperty("password", "masterkey");
		
		this.pool = new ConnectionPool(new DriverManagerConnectionFactory("org.firebirdsql.jdbc.FBDriver",
				"jdbc:firebirdsql:localhost:D:\\Database\\IPPLAN_UP.FDB",prop), 1, 4);
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
	public void closeConnection(Connection arg0) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public Connection getConnection() throws SQLException {
		return this.pool.getConnection();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		// TODO Auto-generated method stub
		return false;
	}

}

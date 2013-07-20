package com.cantor.ipplan.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import com.gelicon.dbcp.ConnectionPool;
import com.gelicon.dbcp.DriverManagerConnectionFactory;

@SuppressWarnings("serial")
public class PoolConnection implements ConnectionProvider {

	private ConnectionPool pool=null;

	
	public void setPool(String dbUrl, String user, String pswd) throws ClassNotFoundException {
		Properties prop = new Properties();
		prop.setProperty("user", (user==null || user.isEmpty())?"SYSDBA":user);
		prop.setProperty("password", (pswd==null || pswd.isEmpty())?"masterkey":pswd);
		//prop.setProperty("encoding", "UNICODE_FSS");
		prop.setProperty("encoding", "UTF8");
		//prop.setProperty("lc_ctype", "WIN1251");
		//prop.setProperty("lc_ctype", "ISO8859_1");
		pool = new ConnectionPool(new DriverManagerConnectionFactory("org.firebirdsql.jdbc.FBDriver",dbUrl,prop), 1, 4);
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return (pool!=null)?pool.getConnection():null;
	}

	@Override
	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

}

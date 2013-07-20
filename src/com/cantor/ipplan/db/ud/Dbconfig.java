package com.cantor.ipplan.db.ud;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@SuppressWarnings("serial")
@Entity
@Table(name = "DBCONFIG")
public class Dbconfig implements java.io.Serializable  {
	
	

	private int dbconfigId;
	private String dbconfigName;
	private String dbconfigValue;

	public Dbconfig() {
	}
	
	@Id
	@Column(name = "DBCONGIG_ID", unique = true, nullable = false)
	public int getDbconfigId() {
		return dbconfigId;
	}

	public void setDbconfigId(int dbconfigId) {
		this.dbconfigId = dbconfigId;
	}

	@Column(name = "DBCONGIG_NAME", nullable = false, length = 100)
	public String getDbconfigName() {
		return dbconfigName;
	}

	public void setDbconfigName(String dbconfigName) {
		this.dbconfigName = dbconfigName;
	}

	@Column(name = "DBCONGIG_VALUE", nullable = true, length = 255)
	public String getDbconfigValue() {
		return dbconfigValue;
	}

	public void setDbconfigValue(String dbconfigValue) {
		this.dbconfigValue = dbconfigValue;
	}

}

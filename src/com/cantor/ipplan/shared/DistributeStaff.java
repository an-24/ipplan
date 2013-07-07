package com.cantor.ipplan.shared;

import java.io.Serializable;
import java.math.BigInteger;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class DistributeStaff  implements Serializable, IsSerializable {
	private int userId;
	private String userLogin;
	private int intValue = 0;
	private double doubleValue = 0.0;
	
	public DistributeStaff(){
		super();
	}

	public int getInt() {
		return intValue;
	}

	public void setInt(Integer count) {
		this.intValue = count==null?0:count;
	}

	public double getDouble() {
		return doubleValue;
	}

	public void setDouble(BigInteger value) {
		this.doubleValue = value==null?0:value.doubleValue();
	}

	public void setDoubleValue(double value) {
		this.doubleValue = value;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}
	
}

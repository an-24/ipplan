package com.cantor.ipplan.shared;

import java.io.Serializable;
import java.math.BigInteger;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class DistributeCost   implements Serializable, IsSerializable {
	private int costsId;
	private String costsName;
	private double doubleValue = 0.0;
	
	public DistributeCost() {
		
	}
	
	
	public int getCostsId() {
		return costsId;
	}
	
	public void setCostsId(int costsId) {
		this.costsId = costsId;
	}
	
	public String getCostsName() {
		return costsName;
	}
	
	public void setCostsName(String costsName) {
		this.costsName = costsName;
	}
	
	public double getDoubleValue() {
		return doubleValue;
	}
	
	public void setDoubleValue(BigInteger value) {
		this.doubleValue = value==null?0:value.doubleValue();
	}

}

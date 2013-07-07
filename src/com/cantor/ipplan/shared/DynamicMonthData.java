package com.cantor.ipplan.shared;

import java.io.Serializable;
import java.math.BigInteger;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class DynamicMonthData  implements Serializable, IsSerializable {

	private int count;
	private int revenue = 0;
	private int costs = 0;
	private int tax = 0;
	private int fine = 0;
	private int statusId;
	private int year;
	private int month;
	
	public DynamicMonthData() {
		super();
	}

	public int getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count==null?0:count;
	}

	public int getRevenue() {
		return revenue;
	}

	public void setRevenue(BigInteger revenue) {
		this.revenue = revenue==null?0:revenue.intValue();
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public int getYear() {
		return year;
	}

	public void setYear(Short year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(Short month) {
		this.month = month;
	}

	public int getCosts() {
		return costs;
	}

	public void setCosts(BigInteger value) {
		this.costs = value==null?0:value.intValue();
	}

	public int getTax() {
		return tax;
	}

	public void setTax(BigInteger value) {
		this.tax = value==null?0:value.intValue();
	}

	public int getFine() {
		return fine;
	}

	public void setFine(BigInteger value) {
		this.fine = value==null?0:value.intValue();
	} 
}

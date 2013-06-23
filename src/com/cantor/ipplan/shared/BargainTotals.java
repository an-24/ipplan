package com.cantor.ipplan.shared;

import java.io.Serializable;
import java.math.BigInteger;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class BargainTotals implements Serializable, IsSerializable {
	private int count;
	private int revenue;
	private int prepayment;
	private int costs;
	private int paymentCosts;
	private int fine;
	private int tax;
	private int statusId;
	
	public BargainTotals() {
		super();
	}

	public int getRevenue() {
		return revenue;
	}

	public void setRevenue(BigInteger revenue) {
		this.revenue = revenue==null?0:revenue.intValue();
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count==null?0:count.intValue();
	}

	public int getPrepayment() {
		return prepayment;
	}

	public void setPrepayment(BigInteger prepayment) {
		this.prepayment = prepayment==null?0:prepayment.intValue();
	}

	public int getCosts() {
		return costs;
	}

	public void setCosts(BigInteger costs) {
		this.costs = costs==null?0:costs.intValue();
	}

	public int getPaymentCosts() {
		return paymentCosts;
	}

	public void setPaymentCosts(BigInteger paymentCosts) {
		this.paymentCosts = paymentCosts==null?0:paymentCosts.intValue();
	}

	public int getFine() {
		return fine;
	}

	public void setFine(BigInteger fine) {
		this.fine = fine==null?0:fine.intValue();
	}

	public int getTax() {
		return tax;
	}

	public void setTax(BigInteger tax) {
		this.tax = tax==null?0:tax.intValue();
	}

	public int getProfit() {
		return revenue - costs - fine - tax;
	}

	public int getMargin() {
		return revenue - costs;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}


}

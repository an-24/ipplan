package com.cantor.ipplan.shared;

import java.util.Date;

import com.google.gwt.user.client.Window;


public class PaymentsWrapper implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable  {
	private int paymentsId;
	private int paymentsPeriod;
	private int paymentsSumma;
	private Date paymentsDate;

	public PaymentsWrapper() {
		
	}
	
	public PaymentsWrapper(int paymentsId, int paymentsPeriod, int paymentsSumma, Date paymentsDate) {
		super();
		this.paymentsId = paymentsId;
		this.paymentsPeriod = paymentsPeriod;
		this.paymentsSumma = paymentsSumma;
		this.paymentsDate = paymentsDate;
	}

	public String getPeriod() {
		String s = String.valueOf(paymentsPeriod);
		String y = s.substring(0, 4);
		int m = new Integer(s.substring(4, 6));
		String sm = new String[]{"январь","февраль","март","апрель","май","июнь",
				                 "июль","август","сентябрь","октябрь","ноябрь", "декабрь"}[m-1];
		return sm+", "+y;
	}
	
	public int getPeriodNumber() {
		return paymentsPeriod;
	}

	public int getPaymentsId() {
		return paymentsId;
	}

	public int getPaymentsSumma() {
		return paymentsSumma;
	}

	public Date getPaymentsDate() {
		return paymentsDate;
	}
	
}

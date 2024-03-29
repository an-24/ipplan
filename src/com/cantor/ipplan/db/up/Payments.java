package com.cantor.ipplan.db.up;

// Generated 13.04.2013 9:53:14 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.cantor.ipplan.core.DataBridge;
import com.cantor.ipplan.shared.PaymentsWrapper;

/**
 * Payments generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "PAYMENTS")
public class Payments implements java.io.Serializable, DataBridge<PaymentsWrapper>  {

	private int paymentsId;
	private PUser puser;
	private int paymentsPeriod;
	private int paymentsSumma;
	private Date paymentsDate;

	public Payments() {
	}

	public Payments(int paymentsId, PUser puser, int paymentsPeriod,
			int paymentsSumma, Date paymentsDate) {
		this.paymentsId = paymentsId;
		this.puser = puser;
		this.paymentsPeriod = paymentsPeriod;
		this.paymentsSumma = paymentsSumma;
		this.paymentsDate = paymentsDate;
	}

	@Id
	@javax.persistence.SequenceGenerator(name="newRec", sequenceName="NEWRECORDID")	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "newRec")
	@Column(name = "PAYMENTS_ID", unique = true, nullable = false)
	public int getPaymentsId() {
		return this.paymentsId;
	}

	public void setPaymentsId(int paymentsId) {
		this.paymentsId = paymentsId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PUSER_ID", nullable = false)
	public PUser getPuser() {
		return this.puser;
	}

	public void setPuser(PUser puser) {
		this.puser = puser;
	}

	@Column(name = "PAYMENTS_PERIOD", nullable = false)
	public int getPaymentsPeriod() {
		return this.paymentsPeriod;
	}

	public void setPaymentsPeriod(int paymentsPeriod) {
		this.paymentsPeriod = paymentsPeriod;
	}

	@Column(name = "PAYMENTS_SUMMA", nullable = false)
	public int getPaymentsSumma() {
		return this.paymentsSumma;
	}

	public void setPaymentsSumma(int paymentsSumma) {
		this.paymentsSumma = paymentsSumma;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "PAYMENTS_DATE", nullable = false, length = 10)
	public Date getPaymentsDate() {
		return this.paymentsDate;
	}

	public void setPaymentsDate(Date paymentsDate) {
		this.paymentsDate = paymentsDate;
	}

	@Override
	public PaymentsWrapper toClient() {
		return new PaymentsWrapper(paymentsId,
				paymentsPeriod,paymentsSumma,paymentsDate);
	}

	@Override
	public void fromClient(PaymentsWrapper data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fetch(boolean deep) {
		// TODO Auto-generated method stub
		
	}

}

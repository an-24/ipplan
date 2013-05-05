package com.cantor.ipplan.shared;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@SuppressWarnings("serial")
public class BargainWrapper implements java.io.Serializable,com.google.gwt.user.client.rpc.IsSerializable {

	public int bargainId;
	public String bargainName;
	public ContractWrapper contract;
	public CustomerWrapper customer;
	public PUserWrapper puser;
	public BargainWrapper bargain;
	public StatusWrapper status;
	public int bargainVer;
	public Date bargainStart;
	public Date bargainFinish;
	public Integer bargainRevenue;
	public Integer bargainPrepayment;
	public Integer bargainCosts;
	public Integer bargainPaymentCosts;
	public Integer bargainFine;
	public Integer bargainTax;
	public Integer bargainHead;
	public Date bargainCreated;
	public CalendarWrapper calendar;
	public Set<BargaincostsWrapper> bargaincostses = new HashSet<BargaincostsWrapper>(0);
	public Set<BargainWrapper> bargains = new HashSet<BargainWrapper>(0);
	public Set<AgreedWrapper> agreeds = new HashSet<AgreedWrapper>(0);
	
	public Attention attention;
	
	public BargainWrapper(){
		super();
		bargainCreated = new Date();
	}
	
	public BargainWrapper(String text) {
		this();
		bargainName = text;
	}

	public String getFullName() {
		return bargainId+". "+bargainName;
	}
	
	public boolean isNew() {
		return bargainId == 0;
	}
}
